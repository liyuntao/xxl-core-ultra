package com.xxl.job.core.enums;

/**
 * Created by xuxueli on 17/5/9.
 */
public enum ExecutorBlockStrategyEnum {

    SERIAL_EXECUTION("Serial execution"),
    /*CONCURRENT_EXECUTION("并行"),*/
    DISCARD_LATER("Discard Later"),
    COVER_EARLY("Cover Early");

    private final String title;

    ExecutorBlockStrategyEnum(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
        if ("DISCARD_LATER".equals(name)) {
            return DISCARD_LATER;
        } else if ("SERIAL_EXECUTION".equals(name)) {
            return SERIAL_EXECUTION;
        } else if ("COVER_EARLY".equals(name)) {
            return COVER_EARLY;
        }
        return defaultItem;
    }
}
