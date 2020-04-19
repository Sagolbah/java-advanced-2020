package ru.ifmo.rain.boger.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {
    private final List<E> data;
    private final Comparator<? super E> comparator;

    public ArraySet() {
        this.data = new ArrayList<>();
        this.comparator = null;
    }

    public ArraySet(Collection<E> data) {
        this.comparator = null;
        this.data = new ArrayList<>(isSorted(data) ? data : new TreeSet<>(data));
    }

    public ArraySet(Collection<E> data, Comparator<? super E> comparator) {
        this.comparator = comparator;
        if (isSorted(data)) {
            this.data = new ArrayList<>(data);
        } else {
            Set<E> newData = new TreeSet<>(comparator);
            newData.addAll(data);
            this.data = new ArrayList<>(newData);
        }
    }

    private ArraySet(List<E> data, Comparator<? super E> comparator) {
        // Invariant: already sorted (called in subSet / uncheckedSegment)
        this.data = data;
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    private boolean isSorted(Collection<E> collection) {
        if (collection.isEmpty()) {
            return true;
        }
        Iterator<E> iter = collection.iterator();
        E cur = iter.next();
        while (iter.hasNext()) {
            E next = iter.next();
            if (compare(cur, next) >= 0) {
                return false;
            }
            cur = next;
        }
        return true;
    }

    private NavigableSet<E> uncheckedSegment(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int left = getIndex(fromElement, fromInclusive ? 0 : 1, 0);
        int right = getIndex(toElement, toInclusive ? 0 : -1, -1);
        if (left > right || left == -1 || right == -1) {
            return emptyArraySet();
        } else {
            return new ArraySet<>(data.subList(left, ++right), comparator);
        }
    }

    @Override
    public E first() {
        checkEmptiness();
        return data.get(0);
    }

    @Override
    public E last() {
        checkEmptiness();
        return data.get(size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        Objects.requireNonNull(o);  // Null is not permitted
        return Collections.binarySearch(this.data, (E) o, comparator) >= 0;
    }

    private E getShiftedElement(final E e, final int shiftIfFound, final int shiftIfNotFound) {
        int index = getIndex(e, shiftIfFound, shiftIfNotFound);
        return index == -1 ? null : data.get(index);
    }

    @Override
    public E lower(E e) {
        return getShiftedElement(e, -1, -1);
    }

    @Override
    public E floor(E e) {
        return getShiftedElement(e, 0, -1);
    }

    @Override
    public E ceiling(E e) {
        return getShiftedElement(e, 0, 0);
    }

    @Override
    public E higher(E e) {
        return getShiftedElement(e, 1, 0);
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(data).iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new ReversedArrayList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        Objects.requireNonNull(fromElement);
        Objects.requireNonNull(toElement);
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        return uncheckedSegment(fromElement, fromInclusive, toElement, toInclusive);
    }

    @SuppressWarnings("unchecked")
    private int compare(E first, E second) {
        if (comparator != null) {
            return comparator.compare(first, second);
        } else if (first instanceof Comparable) {
            return ((Comparable<E>) first).compareTo(second);
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return isEmpty() ? emptyArraySet() : uncheckedSegment(first(), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return isEmpty() ? emptyArraySet() : uncheckedSegment(fromElement, inclusive, last(), true);
    }

    private int getIndex(E e, int shiftIfFound, int shiftIfNotFound) {
        Objects.requireNonNull(e);
        int pos = Collections.binarySearch(data, e, comparator);
        // not found: -ub - 1 = res  <=>  ub = -res - 1
        if (pos < 0) {
            pos = -pos - 1 + shiftIfNotFound;
        } else {
            pos += shiftIfFound;
        }
        return (pos >= 0 && pos < data.size()) ? pos : -1;
    }

    private void checkEmptiness() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
    }

    private NavigableSet<E> emptyArraySet() {
        return new ArraySet<>(new ArrayList<>(), this.comparator);
    }
}
