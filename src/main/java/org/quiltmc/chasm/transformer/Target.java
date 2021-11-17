package org.quiltmc.chasm.transformer;

import org.quiltmc.chasm.tree.Node;

public interface Target {
    /**
     * True if this target fully contains the other
     * @param other
     * @return
     */
    boolean contains(Target other);

    /**
     * True if the targets overlap, but neither fully contains the other
     * @param other
     * @return
     */
    boolean overlaps(Target other);

    Node resolve(Node root);
}
