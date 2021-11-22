package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public class NodeTarget implements Target {
    private final PathMetadata path;

    public NodeTarget(Node target) {
        this.path = target.getMetadata().get(PathMetadata.class);
    }

    public PathMetadata getPath() {
        return path;
    }

    @Override
    public boolean contains(Target other) {
        if (other instanceof NodeTarget) {
            return ((NodeTarget) other).getPath().startsWith(this.path);
        } else if (other instanceof SliceTarget) {
            return ((SliceTarget) other).getPath().startsWith(this.path);
        } else {
            throw new RuntimeException("Unexpected Target Type");
        }
    }

    @Override
    public boolean overlaps(Target other) {
        return false;
    }

    @Override
    public Node resolve(Node root) {
        return path.resolve(root);
    }
}
