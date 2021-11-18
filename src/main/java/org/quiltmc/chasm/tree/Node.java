package org.quiltmc.chasm.tree;

import org.quiltmc.chasm.transformer.NodePath;

public interface Node {
    /**
     * Recursively initialize the path of this node with the given path. Calling this method on a node twice should
     * throw an exception.
     *
     * @param path The path that points to this node.
     */
    void initializePath(NodePath path);

    NodePath getPath();

    Node toImmutable();

    boolean isImmutable();
}
