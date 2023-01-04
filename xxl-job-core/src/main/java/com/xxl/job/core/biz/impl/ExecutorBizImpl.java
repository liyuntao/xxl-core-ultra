package com.xxl.job.core.biz.impl;

import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.executor.JobTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 17/3/1.
 */
public class ExecutorBizImpl implements ExecutorBiz {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorBizImpl.class);

    private final JobTaskManager taskManager;
    public ExecutorBizImpl (JobTaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public JobResponseEntity beat() {
        return JobResponseEntity.SUCCESS;
    }

    @Override
    public JobResponseEntity idleBeat(IdleBeatParam idleBeatParam) {
        var jobId = idleBeatParam.getJobId();
        var isBusy = this.taskManager.isActorIdle(jobId);
        if (isBusy) {
            logger.debug("idleBeat busy. jobId={}", jobId);
            return new JobResponseEntity(JobResponseEntity.FAIL_CODE, "job thread is running or has trigger queue.");
        }
        return JobResponseEntity.SUCCESS;
    }

    @Override
    public JobResponseEntity run(TriggerParam triggerParam) {
        if (logger.isTraceEnabled()) {
            logger.trace("receive run param: {}", triggerParam);
        }

        if ("BEAN".equals(triggerParam.getGlueType())) {
            return this.taskManager.trySubmit(triggerParam);
        } else {
            return new JobResponseEntity(JobResponseEntity.FAIL_CODE,
                    "glueType[" + triggerParam.getGlueType() + "] is not valid.");
        }
    }

    @Override
    public JobResponseEntity kill(KillParam killParam) {
        // FIXME
        return JobResponseEntity.SUCCESS;

        // kill handlerThread, and create new one
//        JobThread jobThread = this.taskManager.loadJobThread(killParam.getJobId());
//        if (jobThread != null) {
//            this.taskManager.removeJobThread(killParam.getJobId(), "scheduling center kill job.");
//            return JobResponseEntity.SUCCESS;
//        }
//        return new JobResponseEntity(JobResponseEntity.SUCCESS_CODE, "job thread already killed.");
    }

    @Override
    public JobResponseEntity log(LogParam logParam) {
//        return new JobResponseEntity(new LogResult(0, 0, "NotSupport", true));
        return JobResponseEntity.SUCCESS;
    }

}
