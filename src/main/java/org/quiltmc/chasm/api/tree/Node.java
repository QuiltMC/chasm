package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

public interface Node {
    /**
     * Creates a deep copy of this {@link Node}.
     * This means that any containing node should also be copied.
     * This also copies the metadata of this Node.
     *
     * @return A recursive copy of this {@link Node}.
     */
    Node copy();

    /**
     * Return the {@link MetadataProvider} of this node.
     *
     * @return The {@link MetadataProvider} of this node.
     */
    @ApiStatus.Internal
    MetadataProvider getMetadata();
}
