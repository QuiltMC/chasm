package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;

public class SliceTarget implements Target {
    private final Node target;
    // NOTE! "Virtual Index". Divide by two for actual list index
    private int startIndex;
    private int endIndex;

    public SliceTarget(ListNode target, int start, int end) {
        this.target = target;
        this.startIndex = start;
        this.endIndex = end;
    }

    public Node getTarget() {
        return target;
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
}
