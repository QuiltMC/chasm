package org.quiltmc.chasm.api.metadata;

import org.quiltmc.chasm.api.tree.Node;

/**
 * {@link Node} metadata, capable of being attached to a {@link MetadataProvider}.
 */
public interface Metadata {
    /**
     * Creates a deep copy of this {@link Metadata}.
     *
     * @return A deep copy of this instance.
     */
    Metadata copy();

    FrozenMetadata freeze();

    default Metadata thaw() {
        return this;
    }
}
