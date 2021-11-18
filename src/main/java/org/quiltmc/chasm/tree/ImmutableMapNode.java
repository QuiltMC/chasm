package org.quiltmc.chasm.tree;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.quiltmc.chasm.transformer.NodePath;

public class ImmutableMapNode extends AbstractMap<String, Node> implements MapNode {
    private final Map<String, Node> inner;
    private final NodePath path;

    public ImmutableMapNode(MapNode mapNode) {
        this.inner = new HashMap<>(mapNode);
        this.path = mapNode.getPath();
    }

    @Override
    public Set<Entry<String, Node>> entrySet() {
        return inner.entrySet();
    }

    @Override
    public void initializePath(NodePath path) {
        throw new UnsupportedOperationException("Can't set path on immutable node.");
    }

    @Override
    public NodePath getPath() {
        return path;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }
}
