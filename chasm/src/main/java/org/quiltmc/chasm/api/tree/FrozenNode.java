package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.internal.metadata.FrozenMetadata;
import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;
import org.quiltmc.chasm.internal.metadata.Metadata;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * This interface marks {@link FrozenNode}s in a CHASM tree.
 *
 * <p> Frozen nodes are immutable, but can be thawed.
 */
public interface FrozenNode extends Node {
    @Override
    Node asMutable();

    @Override
    default FrozenNode asImmutable() {
        return this;
    }

    @Override
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    default MetadataProvider<Metadata> getMetadata() {
        return (MetadataProvider<Metadata>) (Object) getFrozenMetadata();
    }

    @ApiStatus.Internal
    FrozenMetadataProvider getFrozenMetadata();
}
