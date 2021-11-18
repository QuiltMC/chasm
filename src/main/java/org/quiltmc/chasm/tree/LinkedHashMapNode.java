package org.quiltmc.chasm.tree;

import java.util.HashMap;
import org.quiltmc.chasm.transformer.NodePath;

public class LinkedHashMapNode extends HashMap<String, Node> implements MapNode {
    private NodePath path = null;

    @Override
    public void initializePath(NodePath path) {
        if (this.path != null) {
            throw new RuntimeException("Path already initialized.");
        }

        this.path = path;

        for (Entry<String, Node> entry : entrySet()) {
            entry.getValue().initializePath(path.append(entry.getKey()));
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
