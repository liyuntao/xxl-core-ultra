package com.xxl;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties({XxlUltraProperties.class})
@Import(XxlUltraSpringBootConfiguration.class)
public class XxlUltraAutoConfiguration {
    public XxlUltraAutoConfiguration() {
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "com.xxl",
            name = {"enableMetric"},
            havingValue = "true",
            matchIfMissing = true
    )
    public XxlUltraMetricsPostProcessor newXxlMetricPostProcessor(ApplicationContext applicationContext) {
        return new XxlUltraMetricsPostProcessor(applicationContext);
    }
}
