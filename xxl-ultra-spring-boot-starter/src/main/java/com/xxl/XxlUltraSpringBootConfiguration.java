package com.xxl;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.XxlJobSpringExecutor;
import com.xxl.job.core.metrics.MicrometerJobDurationRecorder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(
        proxyBeanMethods = false
)
@ConditionalOnProperty(
        prefix = "com.xxl",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = true
)
public class XxlUltraSpringBootConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XxlJobSpringExecutor xxlJobExecutor(XxlUltraProperties prop) {
        XxlJobExecutor.Builder xxlBuilder = XxlJobExecutor.builder()
                .adminAddresses(prop.adminAddress)
                .appname(prop.appName)
                .port(prop.port)
                .accessToken(prop.accessToken);
        if (prop.enableMetric) {
            xxlBuilder.jobDurationCollector(new MicrometerJobDurationRecorder());
        }
        return xxlBuilder.buildSpringExecutor();
    }
}
