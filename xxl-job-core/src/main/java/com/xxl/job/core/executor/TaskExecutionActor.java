package com.xxl.job.core.executor;

import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.JobResponseEntity;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.metrics.JobDurationRecorder;
import com.xxl.job.core.schedule.BackgroundTaskManager;
import com.xxl.job.core.schedule.XxlScheduler;
import com.xxl.job.core.schedule.XxlThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.lang.reflect.Method;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class TaskExecutionActor {
    private static final Logger log = LoggerFactory.getLogger(TaskExecutionActor.class);

    private static final ExecutorService dispatcher = Executors.newSingleThreadExecutor(
            runnable -> XxlThread.daemon("xxl-dispatcher", runnable));

    public final String name;
    private final Object target;
    private final Method method;
    private final XxlScheduler xxlScheduler;
    private final JobDurationRecorder jdc;

    private final LinkedBlockingQueue<TriggerParam> triggerQueue = new LinkedBlockingQueue<>();
    private volatile CompletableFuture<JobResponseEntity> currentJobFiber = null;
    private long lastTriggerId = 0L;

    public TaskExecutionActor(String name, Object target, Method method,
                              XxlScheduler xxlScheduler, JobDurationRecorder jdc) {
        this.name = name;
        this.target = target;
        this.method = method;
        this.xxlScheduler = xxlScheduler;
        this.jdc = jdc;
    }

    TaskSubmitResult submitTaskUnsafe(TriggerParam triggerParam) {
        try {
            return dispatcher.submit(() -> submitTaskInner(triggerParam)).get();
        } catch (Exception ignore){
            return TaskSubmitResult.SCHEDULED;
        }
    }

    void submitInner(TriggerParam triggerParam) {
        dispatcher.submit(() -> submitTaskInner(triggerParam));
    }

    private TaskSubmitResult submitTaskInner(TriggerParam triggerParam) {
        var jobId = triggerParam.getJobId();
        var triggerId = triggerParam.getLogId();
        var triggerAt = triggerParam.getLogDateTime();

        // debounce
        if (triggerId == lastTriggerId) {
            log.trace("same triggerId, ignore. {}", lastTriggerId);
            return TaskSubmitResult.DEBOUNCED;
        }

        var blockStrategy = ExecutorBlockStrategyEnum.match(
                triggerParam.getExecutorBlockStrategy(),
                ExecutorBlockStrategyEnum.DISCARD_LATER);

        var param = triggerParam.getExecutorParams();

        log.trace("ready to submit task, jobId={}, blockStrategy={}", jobId, blockStrategy);
        switch (blockStrategy) {
            case DISCARD_LATER -> {
                if (isRunningOrQueueNonEmpty()) {
                    log.trace("taskFiberDispatch>>discard>>drop, jobId={}, blockStrategy={}", jobId, blockStrategy);
                     return TaskSubmitResult.DISCARD;
                } else {
                    log.trace("taskFiberDispatch>>discard>>runNow, jobId={}, blockStrategy={}", jobId, blockStrategy);
                    this.currentJobFiber = scheduleJobTask(() -> this.execJobMethodWithTrace(param, triggerId), triggerId, triggerAt, triggerParam.getExecutorTimeout());
                }
            }
            case COVER_EARLY -> {
                log.trace("taskFiberDispatch>>coverOld>>runNow, jobId={}, blockStrategy={}", jobId, blockStrategy);
                tryCancelOld();
                this.currentJobFiber = scheduleJobTask(() -> this.execJobMethodWithTrace(param, triggerId), triggerId, triggerAt, triggerParam.getExecutorTimeout());
            }
            default -> {
                if (isRunningOrQueueNonEmpty()) {
                    log.trace("taskFiberDispatch>>default>>enqueue, jobId={}, blockStrategy={}", jobId, blockStrategy);
                    triggerQueue.add(triggerParam);
                    return TaskSubmitResult.QUEUED;
                } else {
                    log.trace("taskFiberDispatch>>default>>runNow, jobId={}, blockStrategy={}", jobId, blockStrategy);
                    this.currentJobFiber = scheduleJobTask(() -> this.execJobMethodWithTrace(param, triggerId), triggerId, triggerAt, triggerParam.getExecutorTimeout());
                }
            }
        }
        return TaskSubmitResult.SCHEDULED;
    }

    private void scanNext() {
        log.trace("trigger scanNext >> begin.");
        var nextTri = triggerQueue.poll();
        if (nextTri != null) {
            this.submitInner(nextTri);
            log.trace("trigger scanNext >> submit finish");
        } else {
            log.trace("trigger scanNext >> emptyQueue");
        }
    }

    public boolean isRunningOrQueueNonEmpty() {
        return currentJobFiber != null && (!currentJobFiber.isDone() || !triggerQueue.isEmpty());
    }

    private void tryCancelOld() {
        var tmpFuture = this.currentJobFiber;
        if (tmpFuture != null) {
            tmpFuture.cancel(false);
        }
        this.currentJobFiber = null;
    }

    private CompletableFuture<JobResponseEntity> scheduleJobTask(Supplier<JobResponseEntity> taskIO, long triggerId, long triggerAt, int timeout) {
        this.lastTriggerId = triggerId;
        CompletableFuture<JobResponseEntity> future = CompletableFuture.supplyAsync(taskIO, xxlScheduler.getWorkerPool());
        if (timeout > 0) {
            future.orTimeout(timeout, TimeUnit.SECONDS);
        }
        future.whenCompleteAsync((jobResp, err) -> {
            if (err != null) {
                var isTimeout = err instanceof TimeoutException;
                int handlerCode;
                String errorMsg;
                if (isTimeout) {
                    handlerCode = 502;
                    errorMsg = "execution exceed timeout=" + timeout;
                } else {
                    handlerCode = 500;
                    errorMsg = err.getMessage() == null ? "empty" : err.getMessage();
                }
                log.debug("err callback enqueue: triggerId={}, errType={}", triggerId, err.getClass().getSimpleName());
                BackgroundTaskManager.pushCallBack(new HandleCallbackParam(
                        triggerId,
                        triggerAt,
                        handlerCode,
                        errorMsg));
            } else {
                if (future == this.currentJobFiber) {
                    this.currentJobFiber = null;
                }
                log.debug("task callback enqueue: triggerId={}, taskCode={}", triggerId, jobResp.getCode());
                BackgroundTaskManager.pushCallBack(new HandleCallbackParam(
                        triggerId,
                        triggerAt,
                        jobResp.getCode(),
                        jobResp.getMsg()));
            }

            scanNext();
        }, dispatcher);
        return future;
    }

    private JobResponseEntity execJobMethod(String param) {
        try {
            return (JobResponseEntity) method.invoke(target, param);
        } catch (Exception err) {
            return new JobResponseEntity(JobResponseEntity.FAIL_CODE, err.getMessage());
        }
    }

    private final String TRACE_LOG_ID = "TRACE_LOG_ID";
    private JobResponseEntity execJobMethodWithTrace(String param, long xxlTriggerId) {
        MDC.put(TRACE_LOG_ID, this.name + xxlTriggerId);

        long start = System.currentTimeMillis();
        JobResponseEntity resp = this.execJobMethod(param);
        var jobExecStatus = switch (resp.getCode()) {
            case 200 -> "success";
            case 502 -> "timeout";
            default -> "failed";
        };
        long cost = System.currentTimeMillis() - start;
        jdc.recordJobDuration(name, cost, jobExecStatus);

        if (log.isTraceEnabled()) {
            log.trace("recordJobDuration name={} cost={}ms", name, cost);
        }

        MDC.remove(TRACE_LOG_ID);
        return resp;
    }
}