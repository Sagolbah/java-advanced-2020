package ru.ifmo.rain.boger.crawler;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

// Class for perHost restriction monitoring.
public class HostMonitor {
    private int activeTasks;
    private final int limit;
    private final Queue<Runnable> waitingTasks;
    private final ExecutorService downloadersThreadPool;

    public HostMonitor(int limit, ExecutorService pool) {
        this.limit = limit;
        this.downloadersThreadPool = pool;
        waitingTasks = new ArrayDeque<>();
        activeTasks = 0;
    }

    public synchronized void addTask(final Runnable task) {
        if (activeTasks < limit) {
            activeTasks++;
            submitTask(task);
        } else {
            waitingTasks.add(task);
        }
    }

    private synchronized void finishTask() {
        if (waitingTasks.peek() == null) {
            activeTasks--;
        } else {
            submitTask(waitingTasks.poll());
        }
    }

    private synchronized void submitTask(final Runnable task){
        downloadersThreadPool.submit(() -> {
            try {
                task.run();
            } finally {
                finishTask();
            }
        });
    }

}
