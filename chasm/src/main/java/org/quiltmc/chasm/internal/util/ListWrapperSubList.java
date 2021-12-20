/**
 *
 */
package org.quiltmc.chasm.internal.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 *
 */
public class ListWrapperSubList<E, L extends List<E>> implements List<E> {

    protected final L list;
    protected int min;
    protected int max;

    public ListWrapperSubList(L list) {
        super();
        this.list = list;
    }

    public ListWrapperSubList(L list, int min, int max) {
        super();
        this.list = list;
        this.min = min;
        this.max = max;
        // should be 0 <= min < max <= list.size()
        if (min < 0 || min > max || max > list.size()) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int size() {
        return this.max - this.min;
    }

    @Override
    public boolean isEmpty() {
        return this.max > this.min;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = this.min; i < this.max; ++i) {
            if (this.list.get(i).equals(o)) {
                return true;
            }
        }
        return false;
    }

    protected static class ListWrapperSubListWrapperIterator<E, L extends List<E>, W extends ListWrapperSubList<E, L>>
            implements Iterator<E> {
        protected final L list;
            protected int nextIndex;
            protected int max;

            public ListWrapperSubListWrapperIterator(W subList) {
                this(subList, subList.min);
            }

            public ListWrapperSubListWrapperIterator(W subList, int i) {
                this.list = subList.list;
                this.max = subList.max;
                if (i < subList.min || i > this.max) {
                    throw new IllegalArgumentException("Bad iterator start index: " + i);
                }
                this.nextIndex = i;
            }

            @Override
            public boolean hasNext() {
                return this.nextIndex < this.max;
            }

            @Override
            public E next() {
                if (this.nextIndex >= this.max) {
                    throw new NoSuchElementException();
                }
                return this.list.get(this.nextIndex);
            }
        }

    @Override
    public Iterator<E> iterator() {
        return new ListWrapperSubListWrapperIterator<>(this);
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[this.size()];
        for (int i = 0, j = this.min; j < this.max; ++i, ++j) {
            objects[i] = this.list.get(j);
        }
        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        Class<T> componentType = (Class<T>) a.getClass().getComponentType();
        if (a.length < this.size()) {
            a = (T[]) Array.newInstance(componentType, this.size());
        }
        for (int i = 0, j = this.min; j < this.max; ++i, ++j) {
            a[i] = componentType.cast(this.list.get(j));
        }
        return a;
    }

    @Override
    public boolean add(E e) {
        int oldSize = this.list.size();
        this.list.add(this.max, e);
        if (this.list.size() > oldSize) {
            ++this.max;
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (this.list.remove(o)) {
            --this.max;
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c instanceof Set<?> || c.size() < this.size()) {
            for (Object o : this) {
                if (!c.contains(o)) {
                    return false;
                }
            }
        } else {
            for (Object o : c) {
                if (!this.contains(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (this.list.addAll(this.max, c)) {
            this.max += c.size();
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        index = adjustToBounds(index);
        if (this.list.addAll(index, c)) {
            this.max += c.size();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int readIndex = this.min;
        int writeIndex = this.min;
        for (; readIndex < this.max; ++readIndex, ++writeIndex) {
            E r = this.list.get(readIndex);
            if (c.contains(r)) {
                if (readIndex != writeIndex) {
                    this.list.set(writeIndex, this.list.get(readIndex));
                }
            } else {
                ++readIndex;
            }
        }
        return trimWrappedList(readIndex, writeIndex);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int readIndex = this.min;
        int writeIndex = this.min;
        for (; readIndex < this.max; ++readIndex, ++writeIndex) {
            E r = this.list.get(readIndex);
            if (!c.contains(r)) {
                if (readIndex != writeIndex) {
                    this.list.set(writeIndex, this.list.get(readIndex));
                }
            } else {
                ++readIndex;
            }
        }
        return trimWrappedList(readIndex, writeIndex);
    }

    private final boolean trimWrappedList(int readIndex, int writeIndex) {
        this.max = writeIndex;
        for (; readIndex < this.list.size(); ++readIndex, ++writeIndex) {
            this.list.set(writeIndex, this.list.get(readIndex));
        }
        while (this.list.size() > writeIndex) {
            this.list.remove(this.list.size() - 1);
        }
        return readIndex > writeIndex;
    }

    @Override
    public void clear() {
        int readIndex = this.max;
        int writeIndex = this.min;
        this.trimWrappedList(readIndex, writeIndex);
    }

    @Override
    public E get(int index) {
        index = adjustToBounds(index);
        return this.list.get(index);
    }

    private int adjustToBounds(int index) {
        index += this.min;
        if (index < this.min || index > this.max) {
            throw new IndexOutOfBoundsException(index - min);
        }
        return index;
    }

    @Override
    public E set(int index, E element) {
        index = adjustToBounds(index);
        return this.list.set(index, element);
    }

    @Override
    public void add(int index, E element) {
        index = adjustToBounds(index);
        this.list.add(index, element);
    }

    @Override
    public E remove(int index) {
        index = adjustToBounds(index);
        return this.list.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        for (int i = this.min; i < this.max; ++i) {
            if (this.list.get(i).equals(o)) {
                return i - this.min;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = this.max - 1; i >= this.min; --i) {
            if (this.list.get(i).equals(o)) {
                return i - this.min;
            }
        }
        return -1;
    }

    protected static class ListWrapperSubListWrapperListIterator<E, S extends List<E>, L extends ListWrapperSubList<E, S>>
            extends ListWrapperSubListWrapperIterator<E, S, L>
            implements ListIterator<E> {
            private final int min;

            public ListWrapperSubListWrapperListIterator(L subPath) {
                super(subPath);
                this.min = subPath.min;
            }

            public ListWrapperSubListWrapperListIterator(L subPath, int i) {
                super(subPath, i);
                this.min = subPath.min;
            }

            @Override
            public boolean hasPrevious() {
                return this.nextIndex > 0;
            }

            @Override
            public E previous() {
                if (this.nextIndex <= this.min) {
                    throw new NoSuchElementException();
                }
                --this.nextIndex;
                return this.list.get(this.nextIndex);
            }

            @Override
            public int nextIndex() {
                return this.nextIndex - this.min;
            }

            @Override
            public int previousIndex() {
                return this.nextIndex - this.min - 1;
            }

            @Override
            public void remove() {
                int index = this.previousIndex();
                if (index < this.min) {
                    throw new IndexOutOfBoundsException();
                }
                this.list.remove(index);
            }

            @Override
            public void set(E e) {
                int index = this.previousIndex();
                if (index < this.min) {
                    throw new IndexOutOfBoundsException();
                }
                this.list.set(index, e);
            }

            @Override
            public void add(E e) {
                int index = this.nextIndex();
                this.list.add(index, e);
            }

        }

    @Override
    public ListIterator<E> listIterator() {
        return new ListWrapperSubListWrapperListIterator<>(this);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        this.adjustToBounds(index);
        return new ListWrapperSubListWrapperListIterator<>(this, index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        fromIndex += this.min;
        toIndex += this.min;
        // should be this.min <= fromIndex <= toIndex <= this.max
        if (this.min > fromIndex || fromIndex > toIndex || toIndex > this.max) {
            throw new IllegalArgumentException();
        }
        return new ListWrapperSubList<>(this, fromIndex, toIndex);
    }

}
