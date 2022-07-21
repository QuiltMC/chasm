package org.quiltmc.chasm.lang.internal.eval;

import org.quiltmc.chasm.lang.api.ast.Node;

public class NodeReference extends Reference {
    private final Node node;

    public NodeReference(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
