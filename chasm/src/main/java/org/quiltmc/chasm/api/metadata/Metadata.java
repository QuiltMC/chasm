package org.quiltmc.chasm.api.metadata;

/**
 * {@link org.quiltmc.chasm.api.tree.Node} metadata, capable of being attached to a {@link MetadataProvider}.
 */
public interface Metadata {
    /**
     * Creates a deep copy of this {@link Metadata}.
     *
     * @return A deep copy of this instance.
     */
    Metadata copy();
}
