/**
 *
 */
package org.quiltmc.chasm.internal.collection;

import java.util.RandomAccess;

/**
 * A semi-minimal RandomAccess List implementation basis.
 */
public interface SimpleRandomAccessList<E> extends RandomAccess {
    /**
     * Returns the number of elements in this list.
     *
     * @return The size of this list.
     */
    int size();

    /**
     * Checks whether this list is empty.
     *
     * <p>Returns true if this list has no elements, and false otherwise.
     *
     * @return whether this list is empty.
     */
    boolean isEmpty();

    /**
     * Adds an element to the end of this list.
     *
     * @param e The element to add to this list.
     *
     * @return Whether an element was added to this list.
     */
    boolean add(E e);

    /**
     * Inserts the element into this list before the element currently at the index.
     *
     * @param index The index of the element to insert before.
     * @param element The element to insert before the indexed position.
     *
     * @return The
     */
    void add(int index, E element);

    /**
     * Removes the first element equal to the specified Object from this list.
     *
     * @param o The object to remove.
     *
     * @return Whether an element was removed from this list.
     */
    boolean remove(Object o);

    /**
     * Removes all elements from this list.
     */
    void clear();

    /**
     * Returns the element at the indicated position in this list.
     *
     * @param index The position of the element to return
     *
     * @return The element at the passed index
     */
    E get(int index);

    /**
     * Replaces the element at the given index in this list.
     *
     * @param index The index of the element to set.
     *
     * @param element The element to set.
     *
     * @return The old element at that index in this list.
     */
    E set(int index, E element);

    /**
     * Cuts the element at the given position out of this list.
     *
     * @param index The index of the element to extract.
     *
     * @return The element cut from this list.
     */
    E remove(int index);
}
