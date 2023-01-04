package com.xxl.job.core.executor;

import com.xxl.job.core.biz.model.JobResponseEntity;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.metrics.JobDurationRecorder;
import com.xxl.job.core.schedule.XxlScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JobTaskManager {
    private static final Logger logger = LoggerFactory.getLogger(JobTaskManager.class);
    private final Map<String, TaskExecutionActor> jobHandlerRepository = new ConcurrentHashMap<>();
    private final Map<Integer, TaskExecutionActor> jobHandlerRepositoryById = new HashMap<>();

    private final XxlScheduler xxlScheduler;
    private final JobDurationRecorder jdc;

    protected JobTaskManager(XxlScheduler xxlScheduler, JobDurationRecorder jdc) {
        this.xxlScheduler = xxlScheduler;
        this.jdc = jdc;
    }

    public JobResponseEntity trySubmit(TriggerParam triggerParam) {
        var handlerName = triggerParam.getExecutorHandler();
        // preCheck
        TaskExecutionActor taskExecutionActor = jobHandlerRepository.get(handlerName);

        if (taskExecutionActor == null) {
            return new JobResponseEntity(JobResponseEntity.FAIL_CODE, "job handler [" + handlerName + "] not found.");
        }

        var jobId = triggerParam.getJobId();
        jobHandlerRepositoryById.put(jobId, taskExecutionActor);

        return switch (taskExecutionActor.submitTaskUnsafe(triggerParam)) {
            case SCHEDULED, QUEUED-> JobResponseEntity.SUCCESS;
            case DISCARD -> new JobResponseEntity(JobResponseEntity.FAIL_CODE, "block strategy effect：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
            case DEBOUNCED -> new JobResponseEntity(JobResponseEntity.FAIL_CODE, "repeat trigger job, logId:" + triggerParam.getLogId());
        };
    }

    protected void registerJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {
        if (xxlJob == null) {
            return;
        }

        String handlerName = xxlJob.value();
        Class<?> clazz = bean.getClass();
        String methodName = executeMethod.getName();
        if (handlerName.trim().length() == 0) {
            throw new IllegalArgumentException("xxl-job method-jobhandler name invalid, for[" + clazz + "#" + methodName + "] .");
        }
        if (jobHandlerRepository.containsKey(handlerName)) {
            throw new IllegalArgumentException("xxl-job jobhandler[" + handlerName + "] naming conflicts.");
        }
        executeMethod.setAccessible(true);

        // registry jobhandler
        logger.info(">>>>>>>>>>> xxl-job register jobhandler success, name:{}", handlerName);
        jobHandlerRepository.put(handlerName, new TaskExecutionActor(handlerName, bean, executeMethod, xxlScheduler, jdc));
    }

    public boolean isActorIdle(long jobId) {
        TaskExecutionActor taskExecutionActor = jobHandlerRepositoryById.get(jobId);
        return taskExecutionActor == null || !taskExecutionActor.isRunningOrQueueNonEmpty();
    }
}
