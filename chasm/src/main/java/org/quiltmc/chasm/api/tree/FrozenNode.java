package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;

/**
 * This interface marks {@link FrozenNode}s in a CHASM tree.
 *
 * <p> Frozen nodes are immutable, but can be thawed.
 */
public interface FrozenNode extends Node {
    @Override
    Node asMutable();

    default FrozenNode asImmutable() {
        return this;
    }

    @ApiStatus.Internal
    FrozenMetadataProvider getMetadata();
}
