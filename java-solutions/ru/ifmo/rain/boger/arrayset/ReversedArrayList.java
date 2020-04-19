package ru.ifmo.rain.boger.arrayset;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;

public class ReversedArrayList<E> extends AbstractList<E> implements RandomAccess {
    private final List<E> data;

    @Override
    public E get(int index) {
        return data.get(size() - index - 1);
    }

    @Override
    public int size() {
        return data.size();
    }

    public ReversedArrayList(List<E> data) {
        this.data = data;
    }
}
