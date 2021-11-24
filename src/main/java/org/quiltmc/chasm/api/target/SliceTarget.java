package org.quiltmc.chasm.api.target;

import java.util.List;

import org.quiltmc.chasm.api.tree.LinkedListNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

/**
 * Slice a {@link ListNode}, as a {@link Target}.
 *
 * <p>A slice of a list is a contiguous subset of the list, like {@link List#subList}.
 */
public class SliceTarget implements Target {
    private final PathMetadata path;
    // NOTE! "Virtual Index". Divide by two for actual list index
    // TODO: Note why this uses "Virtual Index"s rather than regular indices.
    private int startIndex;
    private int endIndex;

    /**
     * Create a {@link SliceTarget} of the passed {@link ListNode}.
     *
     * @param listNode The {@link ListNode} to slice.
     *
     * @param start The first index of the {@link ListNode} included in the {@link SliceTarget}.
     *
     * @param end The first index after the {@code start} of the {@link ListNode}
     *           not included in the {@link SliceTarget}. 
     */
    public SliceTarget(ListNode listNode, int start, int end) {
        this.path = listNode.getMetadata().get(PathMetadata.class);
        assert this.path != null;
        this.startIndex = start;
        this.endIndex = end;
    }

    /**
     * Get this {@link SliceTarget}'s {@link PathMetadata}.
     *
     * @return The {@link PathMetadata} of this {@link SliceTarget}.
     */
    public PathMetadata getPath() {
        return path;
    }

    /**
     * Get this {@link SliceTarget}'s slice start index.
     *
     * @return The start index of this {@link SliceTarget}' slice.
     */
    public int getStartIndex() {
        return startIndex;
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
        return endIndex;
    }

    /**
     * Set this {@link SliceTarget}'s slice end index. 
     *
     * @param endIndex The new end index for this {@link SliceTarget}'s slice.
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public boolean contains(Target other) {
        if (other instanceof NodeTarget) {
            NodeTarget nodeTarget = (NodeTarget) other;
            PathMetadata targetPath = nodeTarget.getPath();
            if (this.path.startsWith(targetPath)) {
                return false;
            }
            if (nodeTarget.getPath().startsWith(this.path)) {
                PathMetadata.Entry index = targetPath.get(this.path.size());
                if (index.isInteger()) {
                    int intIndex = index.asInteger();
                    return startIndex / 2 <= intIndex && intIndex < endIndex / 2;
                } else {
                    throw new RuntimeException("Unexpected index type");
                }
            }
        }
        return false;
    }

    @Override
    public boolean overlaps(Target other) {
        if (other instanceof NodeTarget) {
            return false;
        } else if (other instanceof SliceTarget) {
            SliceTarget sliceTarget = (SliceTarget) other;
            
            if (!this.path.equals(sliceTarget.path)) {
                return false;
            }

            if (this.startIndex > sliceTarget.startIndex) {
                return other.overlaps(this);
            }

            if (this.startIndex == sliceTarget.startIndex) {
                return false;
            }

            return sliceTarget.startIndex < this.endIndex && this.endIndex < sliceTarget.endIndex;
        } else {
            throw new RuntimeException("Unexpected target type");
        }
    }

    @Override
    public Node resolve(Node root) {
        Node parent = path.resolve(root);
        if (parent instanceof ListNode) {
            ListNode parentList = (ListNode) parent;
            ListNode slice = new LinkedListNode();
            int realStart = startIndex / 2;
            int realEnd = endIndex / 2;
            for (int i = realStart; i < realEnd; i++) {
                slice.add(parentList.get(i));
            }
            return slice;
        } else {
            throw new UnsupportedOperationException("Invalid slice into non-list");
        }
    }
}
