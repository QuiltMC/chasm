package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

/**
 * Navigate to a subtree of a {@link Node}, as a {@link Target}.
 */
public class NodeTarget implements Target {
    private final PathMetadata path;

    /**
     * Make a new {@link NodeTarget} that targets the passed node,
     * using the {@link Node}'s attached {@link PathMetadata}.
     *
     * @param target The {@link Node} to target.
     */
    public NodeTarget(Node target) {
        this.path = target.getMetadata().get(PathMetadata.class);
    }

    /**
     * Get the {@link PathMetadata} of this {@link NodeTarget}.
     *
     * @return This {@link NodeTarget}'s {@link PathMetadata}.
     */
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
