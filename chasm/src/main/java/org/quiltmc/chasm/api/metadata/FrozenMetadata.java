package org.quiltmc.chasm.api.metadata;

/**
 *
 */
public interface FrozenMetadata extends Metadata {
    @Override
    default FrozenMetadata copy() {
        // no reason to copy
        return this;
    }

    @Override
    default FrozenMetadata freeze() {
        return this;
    }

    @Override
    Metadata thaw();
}
