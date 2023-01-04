package com.xxl.job.core.handler;

import com.xxl.job.core.biz.model.JobResponseEntity;


public abstract class AbstractJobHandler {

    public abstract JobResponseEntity execute(String param) throws Exception;

    protected JobResponseEntity handleSuccess() {
        return JobResponseEntity.SUCCESS;
    }

    protected JobResponseEntity handleSuccess(String msg) {
        return new JobResponseEntity(JobResponseEntity.SUCCESS_CODE, msg);
    }

    protected JobResponseEntity handleFail() {
        return JobResponseEntity.FAIL;
    }

    protected JobResponseEntity handleFail(String msg) {
        return new JobResponseEntity(JobResponseEntity.FAIL_CODE, msg);
    }

    protected JobResponseEntity handleTimeout() {
        return JobResponseEntity.TIMEOUT;
    }

    protected JobResponseEntity handleTimeout(String msg) {
        return new JobResponseEntity(JobResponseEntity.TIMEOUT_CODE, msg);
    }
}
