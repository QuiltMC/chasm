package org.quiltmc.chasm.transformer;

import org.quiltmc.chasm.tree.LinkedListNode;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.Node;

public class SliceTarget implements Target {
    private final NodePath path;
    // NOTE! "Virtual Index". Divide by two for actual list index
    private int startIndex;
    private int endIndex;

    public SliceTarget(NodePath path, int start, int end) {
        this.path = path;
        this.startIndex = start;
        this.endIndex = end;
    }

    public NodePath getPath() {
        return path;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public boolean contains(Target other) {
        if (other instanceof NodeTarget) {
            NodeTarget nodeTarget = (NodeTarget) other;
            if (nodeTarget.getPath().contains(this.path)) {
                return false;
            }
            if (this.path.contains(nodeTarget.getPath())) {
                Object index = nodeTarget.getPath().getEntryAt(this.path.getLength());
                if (index instanceof Integer) {
                    int intIndex = (Integer) index;
                    return startIndex / 2 <= intIndex && intIndex < endIndex / 2;
                } else {
                    throw new RuntimeException("Unexpected index type");
                }
            }
            return false;
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
            ListNode slice = new LinkedListNode();
            int realStart = startIndex / 2;
            int realEnd = endIndex / 2;
            for (int i = realStart; i < realEnd; i++) {
                slice.add(((ListNode) parent).get(i));
            }
            return slice;
        } else {
            throw new UnsupportedOperationException("Invalid slice into non-list");
        }
    }
}
