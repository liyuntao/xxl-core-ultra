package com.xxl;

import com.xxl.job.core.executor.XxlJobSpringExecutor;
import com.xxl.job.core.metrics.JobDurationRecorder;
import com.xxl.job.core.metrics.MicrometerJobDurationRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;

public class XxlUltraMetricsPostProcessor implements BeanPostProcessor, Ordered {

    private final ApplicationContext context;
    private MeterRegistry meterRegistry = null;
    private final Logger log = LoggerFactory.getLogger(XxlUltraMetricsPostProcessor.class);

    XxlUltraMetricsPostProcessor(ApplicationContext context) {
        this.context = context;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
//        if (bean instanceof XxlJobSpringExecutor) {
//            JobDurationRecorder jobDurationRecorder = ((XxlJobSpringExecutor) bean).getJobDurationRecorder();
//            if (jobDurationRecorder instanceof MicrometerJobDurationRecorder) {
//                log.info("detect meterRegistry instance. Register to xxl-ultra");
//                ((MicrometerJobDurationRecorder) jobDurationRecorder).injectMeterRegistry(this.getMeterRegistry());
//            }
//        }
        return bean;
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            XxlJobSpringExecutor xxlJobSpringExecutor = context.getBean(XxlJobSpringExecutor.class);
            MeterRegistry registry = context.getBean(MeterRegistry.class);

            var jdr = xxlJobSpringExecutor.getJobDurationRecorder();
            if (jdr instanceof MicrometerJobDurationRecorder) {
                log.info("detect MeterRegistry instance. register to xxl-ultra");
                ((MicrometerJobDurationRecorder) jdr).injectMeterRegistry(registry);
            }
        } catch (Throwable ignore) {

        }
    }

    private MeterRegistry getMeterRegistry() {
        if (this.meterRegistry == null) {
            this.meterRegistry = this.context.getBean(MeterRegistry.class);
        }

        return this.meterRegistry;
    }

    public int getOrder() {
        return Integer.MIN_VALUE;
    }

}
