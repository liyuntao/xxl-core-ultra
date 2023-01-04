package com.xxl.job.core.biz.client;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.JobResponseEntity;
import com.xxl.job.core.util.AsyncHttpUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminBizClient implements AdminBiz {

    public AdminBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        // valid
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
    }

    private String addressUrl ;
    private final String accessToken;
    private final int timeout = 3;


    @Override
    public CompletableFuture<JobResponseEntity> callback(List<HandleCallbackParam> callbackParamList) {
        return AsyncHttpUtil.postBody(addressUrl+"api/callback", accessToken, timeout, callbackParamList);
    }

    @Override
    public CompletableFuture<JobResponseEntity> registry(RegistryParam registryParam) {
        return AsyncHttpUtil.postBody(addressUrl + "api/registry", accessToken, timeout, registryParam);
    }

    @Override
    public CompletableFuture<JobResponseEntity> registryRemove(RegistryParam registryParam) {
        return AsyncHttpUtil.postBody(addressUrl + "api/registryRemove", accessToken, timeout, registryParam);
    }

}
