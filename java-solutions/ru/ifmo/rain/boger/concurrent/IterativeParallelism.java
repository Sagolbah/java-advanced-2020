package ru.ifmo.rain.boger.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class IterativeParallelism implements AdvancedIP {

    private final ParallelMapper mapper;

    // Constructors

    public IterativeParallelism() {
        this.mapper = null;
    }

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    // Utility functions

    private <T, T2, R> R reduce(final int threadsNum, final List<T> values,
                                final Function<Stream<T>, T2> segmentFolder,
                                final Function<Stream<T2>, R> resultFolder) throws InterruptedException {
        if (threadsNum <= 0) {
            throw new IllegalArgumentException("Number of threads must be greater or equal than 1");
        }
        final int blockSize = values.size() / threadsNum;
        final int remainderSize = values.size() % threadsNum;
        List<Stream<T>> streams = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int left = 0;
        for (int i = 0; i < threadsNum; i++) {
            int right = left + blockSize + (remainderSize > i ? 1 : 0);
            if (right != left) {
                streams.add(values.subList(left, right).stream());
            }
            left = right;
        }
        // Creating with specified size - adding with list.add() may cause invalid order
        List<T2> result;
        if (mapper == null) {
            result = new ArrayList<>(Collections.nCopies(streams.size(), null));
            for (int i = 0; i < streams.size(); i++) {
                final int targetIndex = i;
                Thread thread = new Thread(() -> result.set(targetIndex, segmentFolder.apply(streams.get(targetIndex))));
                threads.add(thread);
                thread.start();
            }
            waitForSolution(threads);
        } else {
            result = mapper.map(segmentFolder, streams);
        }
        return resultFolder.apply(result.stream());
    }

    private void waitForSolution(final List<Thread> threads) throws InterruptedException {
        // Waiting till all threads provide solution
        for (Thread thread : threads) {
            thread.join();
        }
    }

    private <T, R> R makeReduction(final Stream<T> stream, final Monoid<R> monoid, final Function<T, R> mapper) {
        return stream.map(mapper).reduce(monoid.getIdentity(), monoid.getOperator());
    }

    private <T> T getGenericMax(Stream<T> stream, Comparator<? super T> comparator) {
        return stream.max(comparator).get();
    }

    // Methods

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Values list must not be empty");
        }
        //final Function<Stream<T>, T> maxFunction = stream -> stream.max(comparator).get();
        return reduce(threads, values, stream -> getGenericMax(stream, comparator), stream -> getGenericMax(stream, comparator));
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
        return !all(threads, values, predicate.negate());
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

    @Override
    public <T> T reduce(int threads, List<T> values, Monoid<T> monoid) throws InterruptedException {
        //final Function<Stream<T>, T> reduction = stream -> stream.reduce(monoid.getIdentity(), monoid.getOperator());
        return reduce(threads, values, stream -> makeReduction(stream, monoid, Function.identity()),
                stream -> makeReduction(stream, monoid, Function.identity()));
    }

    @Override
    public <T, R> R mapReduce(int threads, List<T> values, Function<T, R> lift, Monoid<R> monoid) throws InterruptedException {
        return reduce(threads, values, stream -> makeReduction(stream, monoid, lift),
                stream -> makeReduction(stream, monoid, Function.identity()));
    }
}
