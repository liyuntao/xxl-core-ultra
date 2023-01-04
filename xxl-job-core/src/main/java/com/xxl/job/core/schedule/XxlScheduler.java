package com.xxl.job.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class XxlScheduler {
    private final int threads;
    private final String threadNamePrefix;
    private final boolean daemon = true;

    public XxlScheduler(int threads, String threadNamePrefix) {
        this.threads = threads;
        this.threadNamePrefix = threadNamePrefix;
    }

    private static final Logger logger = LoggerFactory.getLogger(XxlScheduler.class);

    private final AtomicInteger schedulerThreadId = new AtomicInteger(0);
    private ScheduledThreadPoolExecutor executor = null;

    private final AtomicInteger workerThreadId = new AtomicInteger(0);
    private final ExecutorService workerPool = Executors.newCachedThreadPool(
            runnable -> new XxlThread("xxl-worker-" + workerThreadId.getAndIncrement(), runnable, true)
    );

    public void startup() {
        logger.debug("Initializing XxlScheduler");

        synchronized (this) {
            executor = new ScheduledThreadPoolExecutor(threads);
            executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
            executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
            executor.setRemoveOnCancelPolicy(true);
            executor.setThreadFactory(runnable ->
                    new XxlThread(threadNamePrefix + schedulerThreadId.getAndIncrement(), runnable, daemon));
        }
    }

    public void shutdown() throws InterruptedException {
        logger.debug("Shutting down XxlScheduler");
        ScheduledThreadPoolExecutor cachedExecutor = this.executor;
        if (cachedExecutor != null) {
            synchronized (this) {
                cachedExecutor.shutdown();
                this.executor = null;
            }
            cachedExecutor.awaitTermination(5, TimeUnit.SECONDS);
            logger.debug("Shutting down XxlScheduler, finished.");
        }
    }

    private boolean isStarted() {
        synchronized (this) {
            return executor != null;
        }
    }

    public ScheduledFuture<?> scheduleOnce(String name, Runnable runnable) {
        return this.schedule(name, runnable, 0, -1, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture<?> schedule(String name, Runnable runnable, long delay, long period, TimeUnit unit) {
        logger.debug("Scheduling task {} with initial delay {} ms and period {} ms.",
                name, TimeUnit.MILLISECONDS.convert(delay, unit), TimeUnit.MILLISECONDS.convert(period, unit));
        synchronized (this) {
            if (isStarted()) {
                if (period >= 0) {
                    return executor.scheduleAtFixedRate(runnable, delay, period, unit);
                } else {
                    return executor.schedule(runnable, delay, unit);
                }
            } else {
                logger.info("Xxl scheduler is not running at the time task '{}' is scheduled. The task is ignored.", name);
                return new NoOpScheduledFutureTask();
            }
        }
    }

    public ExecutorService getWorkerPool() {
        return this.workerPool;
    }

}
