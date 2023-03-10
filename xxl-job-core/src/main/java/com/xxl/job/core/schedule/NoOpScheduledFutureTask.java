package com.xxl.job.core.schedule;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class NoOpScheduledFutureTask implements ScheduledFuture<Object> {
    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public int compareTo(Delayed o) {
        long diff = getDelay(NANOSECONDS) - o.getDelay(NANOSECONDS);
        if (diff < 0) {
            return -1;
        } else if (diff > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}
