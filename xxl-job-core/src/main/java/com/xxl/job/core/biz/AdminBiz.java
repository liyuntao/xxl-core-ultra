package com.xxl.job.core.biz;

import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.JobResponseEntity;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public interface AdminBiz {
    CompletableFuture<JobResponseEntity> callback(List<HandleCallbackParam> callbackParamList);

    CompletableFuture<JobResponseEntity> registry(RegistryParam registryParam);

    CompletableFuture<JobResponseEntity> registryRemove(RegistryParam registryParam);
}
