package ru.ifmo.rain.boger.crawler;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class HostMonitor {
    private int activeTasks;
    private int limit;
    private Queue<Runnable> waitingTasks;
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
            downloadersThreadPool.submit(task);
        } else {
            waitingTasks.add(task);
        }
    }

    public synchronized void finishTask() {
        if (waitingTasks.peek() == null) {
            activeTasks--;
        } else {
            downloadersThreadPool.submit(waitingTasks.poll());
        }
    }

}
