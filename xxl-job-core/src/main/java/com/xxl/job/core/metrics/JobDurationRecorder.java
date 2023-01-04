package com.xxl.job.core.metrics;


@FunctionalInterface
public interface JobDurationRecorder {

    default boolean isEnabled() {
        return true;
    }

    void recordJobDuration(String jobName, long jobDuration, String jobResult);

    static JobDurationRecorder disabled() {

        return new JobDurationRecorder() {

            @Override
            public boolean isEnabled() {
                return false;
            }

            @Override
            public void recordJobDuration(String jobName, long jobDuration, String jobResult) {
            }

        };
    }
}
