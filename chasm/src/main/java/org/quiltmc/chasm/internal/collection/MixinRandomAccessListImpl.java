/**
 *
 */
package org.quiltmc.chasm.internal.collection;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 *
 */
public interface MixinRandomAccessListImpl<E> extends SimpleRandomAccessList<E>, List<E> {
    @Override
    default boolean contains(Object element) {
        return this.lastIndexOf(element) > 0;
    }



    @Override
    default Iterator<E> iterator() {
        return new MixinRandomAccessListImplIterator<>(this);
    }

    @Override
    default Object[] toArray() {
        Object[] out = new Object[this.size()];
        for (int i = this.size() - 1; i >= 0; --i) {
            out[i] = this.get(i);
        }
        return out;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <T> T[] toArray(T[] a) {
        Class<T[]> arrayClass = (Class<T[]>) a.getClass();
        Class<T> componentClass = (Class<T>) arrayClass.getComponentType();
        if (a.length < this.size()) {
            a = arrayClass.cast(Array.newInstance(componentClass, 0));
        }
        for (int i = this.size() - 1; i >= 0; --i) {
            a[i] = componentClass.cast(this.get(i));
        }
        return a;
    }

    @Override
    default boolean containsAll(Collection<?> c) {
        if (c instanceof Set<?> || c.size() > this.size()) {
            for (Object obj : c) {
                if (!this.contains(obj)) {
                    return false;
                }
            }
        } else {
            for (int i = this.size() - 1; i >= 0; --i) {
                if (!c.contains(this.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    default boolean addAll(Collection<? extends E> c) {
        boolean changed = false;
        for (E element : c) {
            changed |= this.add(element);
        }
        return changed;
    }

    @Override
    default boolean addAll(final int index, Collection<? extends E> c) {
        if (index < 0 || index > this.size()) {
            throw new IndexOutOfBoundsException(index);
        }
        final int oldListLength = this.size();
        final int insertingElementCount = c.size();
        // final int insertedElementEndIndex = index + insertingElementCount;
        boolean changed = false;
        // inserting HIJK after the F in
        // ABCDEFGLMNOP
        // ABCDEFGHIJKLMNOP
        // =======^^^^>++++
        // = -> same
        // ^ -> inserted
        // > -> moved right
        // + -> added
        final int copyElementStartIndex = oldListLength - insertingElementCount;
        for (int i = copyElementStartIndex; i < oldListLength; ++i) {
            changed |= this.add(this.get(copyElementStartIndex));
        }
        // final int naiveMoveStartIndex = index
        // final int naiveMoveEndIndex = oldListLength
        final int moveElementEndIndex = oldListLength - insertingElementCount;
        for (int readIndex = moveElementEndIndex - 1,
                writeIndex = oldListLength - 1; readIndex >= index; --readIndex, --writeIndex) {
            E read = this.get(readIndex);
            changed |= this.set(writeIndex, read) != read;
        }
        return changed;
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        int differPosition = this.size();
        for (int i = 0; i < this.size(); ++i) {
            if (c.contains(this.get(i))) {
                differPosition = i;
                break;
            }
        }
        if (differPosition == this.size()) {
            return false;
        }
        int writeIndex = differPosition;
        int readIndex = differPosition + 1;
        while (readIndex < this.size()) {
            if (!c.contains(this.get(readIndex))) {
                this.set(writeIndex, this.get(readIndex));
                ++writeIndex;
            }
            ++readIndex;
        }
        for (int i = this.size() - 1; i > writeIndex; --i) {
            this.remove(i);
        }
        return true;
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        int differPosition = this.size();
        for (int i = 0; i < this.size(); ++i) {
            if (!c.contains(this.get(i))) {
                differPosition = i;
                break;
            }
        }
        if (differPosition == this.size()) {
            return false;
        }
        int writeIndex = differPosition;
        int readIndex = differPosition + 1;
        while (readIndex < this.size()) {
            if (c.contains(this.get(readIndex))) {
                this.set(writeIndex, this.get(readIndex));
                ++writeIndex;
            }
            ++readIndex;
        }
        for (int i = this.size() - 1; i > writeIndex; --i) {
            this.remove(i);
        }
        return true;
    }

    @Override
    default void replaceAll(UnaryOperator<E> op) {
        for (int i = 0; i < this.size(); ++i) {
            this.set(i, op.apply(this.get(i)));
        }
    }

    @Override
    default int indexOf(Object o) {
        for (int i = 0; i < this.size(); ++i) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default int lastIndexOf(Object o) {
        for (int i = this.size() - 1; i >= 0; --i) {
            if (this.get(i).equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    default ListIterator<E> listIterator() {
        return new MixinRandomAccessListImplListIterator<E>(this);
    }

    @Override
    default ListIterator<E> listIterator(int i) {
        if (i < 0 || i > this.size()) {
            throw new IndexOutOfBoundsException(i);
        }
        return new MixinRandomAccessListImplListIterator<>(this, i);
    }

    @Override
    default List<E> subList(int min, int max) {
        return new ListWrapperSubList<>(this, min, max);
    }

    /**
     * Checks the equality of this and another List.
     *
     * @param other The other list to compare to.
     *
     * @return Whether these two lists have the same elements in the same order.
     *
     * @see List#equals
     */
    default boolean listEqualsHelper(List<?> other) {
        if (this.size() != other.size() || this.hashCode() != other.hashCode()) {
            return false;
        }

        for (int i = 0; i < other.size(); ++i) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates the standard hashcode of this list.
     *
     * @return The standard hashcode of this list.
     *
     * @see List#hashCode()
     */
    default int listHashcodeHelper() {
        int hashCode = 1;
        for (int i = 0; i < this.size(); ++i) {
            E e = this.get(i);
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }
}
