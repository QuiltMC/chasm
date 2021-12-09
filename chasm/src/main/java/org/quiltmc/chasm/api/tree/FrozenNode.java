package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;

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
    default MetadataProvider getMetadata() { return getFrozenMetadata(); }

    FrozenMetadataProvider getFrozenMetadata();
}
