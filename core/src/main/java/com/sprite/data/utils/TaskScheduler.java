package com.sprite.data.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {


    /**
     * Private constructor to prevent instantiation
     */
    private TaskScheduler() {
        throw new IllegalAccessError("Utility class");
    }

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Schedules a task to run asynchronously
     *
     * @param task  The task to run
     * @param delay The delay before the task runs
     * @param unit  The unit of time for the delay
     */
    public static void scheduleAsyncTask(Runnable task, long delay, TimeUnit unit) {
        scheduler.schedule(task, delay, unit);
    }

    /**
     * Shuts down the task scheduler
     */
    public static void shutdown() {
        scheduler.shutdown();
    }
}
