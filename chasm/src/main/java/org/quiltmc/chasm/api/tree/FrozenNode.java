package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;

/**
 * This interface marks {@link FrozenNode}s in a CHASM tree.
 *
 * <p> Frozen nodes are immutable, but can be thawed.
 */
public interface FrozenNode extends Node {
    @Override
    default boolean isMutable() { return false; }

    @Override
    default boolean isImmutable() { return true; }

    @Override
    default Node asMutable() {
        return asMutableCopy();
    }

    @Override
    default FrozenNode asImmutable() {
        return this;
    }

    @Override
    Node asMutableCopy();

    @Override
    FrozenMetadataProvider getMetadata();
}
