package com.xxl;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "com.xxl"
)
public class XxlUltraProperties {
    /**
     *  全局控制是否启用 xxl 执行器，默认为 true
     *  本地调试可设置为 false
     */
    boolean enabled = true;

    /**
     * 执行器名称，注册至 xxl-admin-web 的「执行器管理」页面下
     * e.g. quote-query-job-executor
     */
    String appName;

    /**
     * 本地暴露的端口，用于 xxl-admin 通过 http 回调触发
     */
    int port = 9990;

    /**
     * xxl-admin 的 http 地址，默认指向对应容器环境的 xxl-admin 服务
     */
    String adminAddress = "http://xxl-job-admin-v2.component.svc.cluster.local:8080/xxl-job-admin";

    /**
     * xxl-admin 的 accessToken, 必填
     */
    String accessToken;

    /**
     * 脚手架使用 actuator 时，建议启用
     * 当前会自动监控 task 执行耗时
     */
    boolean enableMetric = false;

    public XxlUltraProperties() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAdminAddress() {
        return adminAddress;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isEnableMetric() {
        return enableMetric;
    }

    public void setEnableMetric(boolean enableMetric) {
        this.enableMetric = enableMetric;
    }
}
