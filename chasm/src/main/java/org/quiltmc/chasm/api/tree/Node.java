package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.internal.metadata.Metadata;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

/**
 * Mark a compliant {@link Node} of a CHASM tree with this interface.
 */
public interface Node {
    /**
     * Get an immutable version of this {@link Node}.
     *
     * <p>This means that all children and {@link Metadata} of the returned
     * {@link Node} must also be immutable.
     * Only immutable children are allowed in order to avoid callers mutating using saved references.
     *
     * @return An immutable version of this {@link Node}.
     */
    FrozenNode asImmutable();

    /**
     * Get a mutable version of this {@link Node}.
     *
     * @return A mutable version of the current {@link Node}.
     */
    default Node asMutable() {
        return this;
    }

    /**
     * Return the {@link MetadataProvider} of this node.
     *
     * @return The {@link MetadataProvider} of this node.
     */
    @ApiStatus.Internal
    MetadataProvider<Metadata> getMetadata();

    @ApiStatus.Internal
    default Node updatePath(PathMetadata newPath) {
        MetadataProvider<Metadata> metadataProvider = getMetadata();
        metadataProvider.put(newPath);

        return this;
    }
}
