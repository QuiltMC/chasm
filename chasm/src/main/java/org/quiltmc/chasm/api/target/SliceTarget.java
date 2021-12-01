package org.quiltmc.chasm.api.target;

import java.util.List;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;

/**
 * Slice a {@link ListNode}, as a {@link Target}.
 *
 * <p>A slice of a list is a contiguous subset of the list, like
 * {@link List#subList}.
 */
public class SliceTarget implements Target {
    private final Node target;
    // NOTE! "Virtual Index". Divide by two for actual list index
    private int startIndex;
    private int endIndex;

    /**
     * Create a {@link SliceTarget} of the passed {@link ListNode}.
     *
     * @param listNode The {@link ListNode} to slice.
     *
     * @param start The first index of the {@link ListNode} included in the
     *            {@link SliceTarget}.
     *
     * @param end The first index after the {@code start} of the {@link ListNode}
     *            not included in the {@link SliceTarget}.
     */
    public SliceTarget(ListNode target, int start, int end) {
        this.target = target;
        this.startIndex = start;
        this.endIndex = end;
    }

    @Override
    public Node getTarget() {
        return this.target;
    }

    /**
     * Get this {@link SliceTarget}'s slice start index.
     *
     * @return The start index of this {@link SliceTarget}' slice.
     */
    public int getStartIndex() {
        return this.startIndex;
    }

    /**
     * Set this {@link SliceTarget}'s slice start index.
     *
     * @param startIndex The new start index for this {@link SliceTarget}'s slice.
     */
    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Get this {@link SliceTarget}'s slice end index.
     *
     * @return The end index of this {@link SliceTarget}'s slice.
     */
    public int getEndIndex() {
        return this.endIndex;
    }

    /**
     * Set this {@link SliceTarget}'s slice end index.
     *
     * @param endIndex The new end index for this {@link SliceTarget}'s slice.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}
