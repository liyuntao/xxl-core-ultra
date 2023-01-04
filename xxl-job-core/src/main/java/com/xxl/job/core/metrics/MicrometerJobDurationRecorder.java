package com.xxl.job.core.metrics;

import com.xxl.job.core.util.XxlAssert;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MicrometerJobDurationRecorder implements JobDurationRecorder {

    static final String LABEL_HANDLER_NAME = "handler";
    static final String METRIC_DURATION = "xxl.job.duration";

    private MeterRegistry meterRegistry = null;

    private final Map<String/*JobId*/, Timer> jobCompletionTimers = new ConcurrentHashMap<>();

    public MicrometerJobDurationRecorder() {
    }

    public void injectMeterRegistry(MeterRegistry meterRegistry) {
        XxlAssert.notNull(meterRegistry, "MeterRegistry must not be null");
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean isEnabled() {
        return meterRegistry != null;
    }

    @Override
    public void recordJobDuration(String jobHandlerName, long jobDuration, String jobResult) {
        if (!isEnabled()) {
            return;
        }

        Timer completionTimer = jobCompletionTimers.computeIfAbsent(jobHandlerName, this::durationTimer);
        completionTimer.record(jobDuration, TimeUnit.MILLISECONDS);
    }

    protected Timer durationTimer(String jobHandlerName) {
        Timer.Builder timer = Timer.builder(METRIC_DURATION)
                .description("job task exec duration(succ)")
                .tag(LABEL_HANDLER_NAME, jobHandlerName);
//                .tags(options.tags());

//        if (options.isHistogram()) {
//            timer.publishPercentileHistogram().publishPercentiles(options.targetPercentiles())
//                    .minimumExpectedValue(options.minLatency()).maximumExpectedValue(options.maxLatency());
//        }

        return timer.register(meterRegistry);
    }
}
