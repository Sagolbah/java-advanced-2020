package ru.ifmo.rain.boger.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class IterativeParallelism implements ListIP {

    // Utility functions

    private <T, R> R reduce(final int threadsNum, final List<? extends T> values,
                            final Function<Stream<? extends T>, ? extends R> segmentFolder,
                            final Function<Stream<? extends R>, ? extends R> resultFolder) throws InterruptedException {
        if (threadsNum <= 0) {
            throw new IllegalArgumentException("Number of threads must be greater or equal than 1");
        }
        final int BLOCK_SIZE = values.size() / threadsNum;
        final int REMAINDER_SIZE = values.size() % threadsNum;
        List<Stream<? extends T>> streams = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int left = 0;
        for (int i = 0; i < threadsNum; i++) {
            int right = left + BLOCK_SIZE + (REMAINDER_SIZE > i ? 1 : 0);
            if (right != left) {
                streams.add(values.subList(left, right).stream());
            }
            left = right;
        }
        // Creating with specified size - adding with list.add() may cause invalid order
        List<R> result = new ArrayList<>(Collections.nCopies(streams.size(), null));
        for (int i = 0; i < streams.size(); i++) {
            final int TARGET_INDEX = i;
            Thread thread = new Thread(() -> result.set(TARGET_INDEX, segmentFolder.apply(streams.get(TARGET_INDEX))));
            threads.add(thread);
            thread.start();
        }
        waitForSolution(threads);
        return resultFolder.apply(result.stream());
    }

    private void waitForSolution(final List<Thread> threads) throws InterruptedException {
        // Waiting till all threads provide solution
        for (Thread thread : threads) {
            thread.join();
        }
    }

    // Methods

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Values list must not be empty");
        }
        final Function<Stream<? extends T>, ? extends T> maxFunction = stream -> stream.max(comparator).get();
        return reduce(threads, values, maxFunction, maxFunction);
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return reduce(threads, values, stream -> stream.allMatch(predicate), stream -> stream.allMatch(Boolean::booleanValue));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, value -> !predicate.test(value));
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return reduce(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()),
                listStream -> listStream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return reduce(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()),
                listStream -> listStream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return reduce(threads, values, stream -> stream.map(f).collect(Collectors.toList()),
                listStream -> listStream.flatMap(Collection::stream).collect(Collectors.toList()));
    }
}
