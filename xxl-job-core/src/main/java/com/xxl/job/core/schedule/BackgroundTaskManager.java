package com.xxl.job.core.schedule;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.JobResponseEntity;
import com.xxl.job.core.enums.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.xxl.job.core.executor.XxlJobExecutor.getAdminBizList;


public class BackgroundTaskManager {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundTaskManager.class);

    private final String appname;
    private final String address;

    public BackgroundTaskManager(String appname, String address) {
        this.appname = appname;
        this.address = address;
    }

    public void execRegister() {
        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appname, address);
        for (AdminBiz adminBiz : getAdminBizList()) {
            adminBiz.registry(registryParam)
                    .thenAccept(registryResult -> {
                        if (JobResponseEntity.SUCCESS_CODE == registryResult.getCode()) {
                            logger.debug(">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}", registryParam, registryResult);
                        } else {
                            logger.warn(">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}", registryParam, registryResult);
                        }
                    }).whenComplete((unused, err) -> {
                        if (err != null) {
                            logger.warn(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, err);
                        }
                    });
        }
    }

    public void execUnRegister() {
        logger.debug("xxl shutdown, exec un-register");
        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), appname, address);
        for (AdminBiz adminBiz : getAdminBizList()) {
            adminBiz.registryRemove(registryParam)
                    .thenAccept(registryResult -> {
                        if (JobResponseEntity.SUCCESS_CODE == registryResult.getCode()) {
                            logger.info(">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}", registryParam, registryResult);
                        } else {
                            logger.warn(">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}", registryParam, registryResult);
                        }
                    }).whenComplete((unused, err) -> {
                        if (err != null) {
                            logger.warn(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam, err);
                        }
                    });
        }
    }

    //// callback ////
    private static final LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<>();

    public static void pushCallBack(HandleCallbackParam callback) {
        callBackQueue.add(callback);
        logger.trace(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    public static void pushCallBackAsync(HandleCallbackParam callback) {
        // TODO
        logger.trace(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    public void syncJobResultToAdmin() {
        try {
            HandleCallbackParam callback = this.callBackQueue.take();

            // callback list param
            List<HandleCallbackParam> callbackParamList = new ArrayList<>();
            int drainToNum = this.callBackQueue.drainTo(callbackParamList);
            callbackParamList.add(callback);

            // callback, will retry if error
            doCallback(callbackParamList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        for (AdminBiz adminBiz : getAdminBizList()) {
            adminBiz.callback(callbackParamList)
                    .thenAccept(callbackResult -> {
                        if (JobResponseEntity.SUCCESS_CODE == callbackResult.getCode()) {
                            logger.debug("xxl-job job callback finish.  param: {}", callbackParamList);
                        } else {
                            logger.warn("xxl-job job callback fail, callbackResult: {},  param: {}", callbackResult, callbackParamList);
                        }
                    }).whenComplete((unused, err) -> {
                        if (err != null) {
                            logger.error("xxl-job job callback error, errorMsg:" + err.getMessage());
                        }
                    });
        }
    }
}
