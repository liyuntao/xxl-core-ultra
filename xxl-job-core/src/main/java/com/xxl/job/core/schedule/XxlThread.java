package com.xxl.job.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XxlThread extends Thread {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static XxlThread daemon(final String name, Runnable runnable) {
        return new XxlThread(name, runnable, true);
    }

    public static XxlThread nonDaemon(final String name, Runnable runnable) {
        return new XxlThread(name, runnable, false);
    }

    public XxlThread(final String name, boolean daemon) {
        super(name);
        configureThread(name, daemon);
    }

    public XxlThread(final String name, Runnable runnable, boolean daemon) {
        super(runnable, name);
        configureThread(name, daemon);
    }

    private void configureThread(final String name, boolean daemon) {
        setDaemon(daemon);
        setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in thread '{}':", name, e));
    }

}
