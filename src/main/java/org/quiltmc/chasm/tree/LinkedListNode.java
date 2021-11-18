package org.quiltmc.chasm.tree;

import java.util.LinkedList;
import org.quiltmc.chasm.transformer.NodePath;

public class LinkedListNode extends LinkedList<Node> implements ListNode {
    private NodePath path = null;

    @Override
    public void initializePath(NodePath path) {
        if (this.path != null) {
            throw new RuntimeException("Path already initialized.");
        }

        this.path = path;

        for (int i = 0; i < this.size(); i++) {
            get(i).initializePath(path.append(i));
        }
    }

    @Override
    public NodePath getPath() {
        return path;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }
}
