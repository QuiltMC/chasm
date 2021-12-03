package org.quiltmc.chasm.internal.tree.frozencollection;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class ImmutableArrayList<T> extends AbstractImmutableCollection<T>
        implements List<T> {

    private T[] elements;

    public ImmutableArrayList(T[] elements) {
        this.elements = elements.clone();
    }


    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean isEmpty() {
        return elements.length > 0;
    }

    @Override
    public boolean contains(Object o) {
        for (T child : elements) {
            if (child.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new FrozenArrayIterator<>(elements);
    }

    @Override
    public Object[] toArray() {
        return elements.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] a) {
        if (a.length < elements.length) {
            Class<? extends Object> aClass = a.getClass();
            a = (E[]) Array.newInstance(aClass.componentType(), elements.length);
        }
        System.arraycopy(elements, 0, a, 0, elements.length);
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Collection<?> t = this;
        if (c.size() < t.size()) {
            Collection<?> o = t;
            t = c;
            c = o;
        }
        for (Object o : t) {
            if (!c.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public T get(int index) {
        return elements[index];
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < elements.length; ++i) {
            if (elements[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = elements.length - 1; i >= 0; --i) {
            if (elements[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ListIterator<T> listIterator() {
        return new FrozenArrayListIterator<>(elements);
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return new FrozenArrayListIterator<>(elements, index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        T[] range = Arrays.copyOfRange(elements, fromIndex, toIndex);
        return newList(range);
    }

    protected ImmutableArrayList<T> newList(T[] range) {
        return new ImmutableArrayList<>(range);
    }
}
