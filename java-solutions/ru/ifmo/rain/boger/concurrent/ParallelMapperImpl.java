package ru.ifmo.rain.boger.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ParallelMapperImpl implements ParallelMapper {

    private static final int TASKS_LIMIT = 1024;
    private final List<Thread> workers;
    private final Queue<Runnable> tasks;

    public ParallelMapperImpl(int threads) {
        workers = Stream.generate(() -> new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    work();
                }
            } catch (InterruptedException e) {
                // No operations.
            }
        })).limit(threads).collect(Collectors.toList());
        tasks = new ArrayDeque<>();
        workers.forEach(Thread::start);
    }

    private void work() throws InterruptedException {
        Runnable currentTask;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                // Idle state - waiting for new task.
                tasks.wait();
            }
            // Found new task - getting it and call other threads to get the job.
            currentTask = tasks.poll();
            tasks.notifyAll();
        }
        currentTask.run();
    }

    private void addTask(Runnable task) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() > TASKS_LIMIT) {
                tasks.wait();
            }
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        ResultList<R> result = new ResultList<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int location = i;
            addTask(() -> {
                try {
                    result.set(location, f.apply(args.get(location)));
                } catch (RuntimeException e) {
                    result.setException(e);
                }
            });
        }
        return result.getData();
    }

    @Override
    public void close() {
        workers.forEach(Thread::interrupt);
        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                // No operations.
            }
        }
    }

    private static class ResultList<T> {
        private int determinedValues = 0;
        private final List<T> data;
        private RuntimeException exception = null;

        public ResultList(int size) {
            data = new ArrayList<>(Collections.nCopies(size, null));
        }

        public synchronized List<T> getData() throws InterruptedException {
            while (determinedValues != data.size()) {
                wait();
            }
            if (exception != null) {
                throw new RuntimeException("A runtime error occurred during mapping execution", exception);
            }
            return data;
        }

        public synchronized void set(int index, T value) {
            determinedValues++;
            data.set(index, value);
            if (determinedValues == data.size()) {
                notifyAll();
            }
        }

        public synchronized void setException(RuntimeException e) {
            exception = e;
        }
    }

}
