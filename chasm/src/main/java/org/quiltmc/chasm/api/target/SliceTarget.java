package org.quiltmc.chasm.api.target;

import java.util.List;

import org.quiltmc.chasm.api.Lock;
import org.quiltmc.chasm.api.tree.ListNode;

/**
 * Slices a {@link ListNode}, as a {@link Target}.
 *
 * <p>A slice of a list is a contiguous subset of the list, like
 * {@link List#subList}.
 */
public class SliceTarget extends Target {
    // NOTE! "Virtual Index". Divide by two for actual list index
    private int startIndex;
    private int endIndex;

    /**
     * Creates a {@link SliceTarget} of the passed {@link ListNode}.
     *
     * @param target The {@code ListNode} to slice.
     *
     * @param start The first index of the {@code ListNode} included in the
     *            {@code SliceTarget}.
     *
     * @param end The first index after the {@code start} of the {@link ListNode}
     *            not included in the {@code SliceTarget}.
     *
     */
    public SliceTarget(ListNode target, int start, int end) {
        super(target, Lock.NONE);
        startIndex = start;
        endIndex = end;
    }

    /**
     * Creates a {@link SliceTarget} of the passed {@link ListNode} and applies the specified {@link Lock}.
     *
     * @param target The {@code ListNode} to slice.
     *
     * @param start The first index of the {@code ListNode} included in the
     *            {@code SliceTarget}.
     *
     * @param end The first index after the {@code start} of the {@link ListNode}
     *            not included in the {@code SliceTarget}.
     *
     * @param lock The {@link Lock} to apply.
     */
    public SliceTarget(ListNode target, int start, int end, Lock lock) {
        super(target, lock);
        startIndex = start;
        endIndex = end;
    }

    /**
     * Gets this {@link SliceTarget}'s slice start index.
     *
     * <p>The slice start index is the first index of the {@link ListNode} included in this {@code SliceTarget}.
     *
     * @return The start index of this {@code SliceTarget}' slice.
     */
    public int getStartIndex() {
        return startIndex;
    }

    /**
     * Sets this {@link SliceTarget}'s slice start index.
     *
     * <p>The slice start index is the first index of the {@link ListNode} included in this {@code SliceTarget}.
     *
     * @param startIndex The new start index for this {@code SliceTarget}'s slice.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Gets this {@link SliceTarget}'s slice end index.
     *
     * <p>The slice end index is the first index after the {@code start} of the {@link ListNode}
     * not included in this {@code SliceTarget}.
     *
     * @return The end index of this {@code SliceTarget}'s slice.
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * Set this {@link SliceTarget}'s slice end index.
     *
     * <p>The slice end index is the first index after the {@code start} of the {@link ListNode}
     * not included in this {@code SliceTarget}.
     *
     * @param endIndex The new end index for this {@code SliceTarget}'s slice.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}
