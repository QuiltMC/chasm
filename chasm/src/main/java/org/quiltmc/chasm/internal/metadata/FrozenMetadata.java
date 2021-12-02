package org.quiltmc.chasm.internal.metadata;

/**
 *
 */
public interface FrozenMetadata extends Metadata {
    @Override
    default FrozenMetadata freeze() {
        return this;
    }

    @Override
    Metadata thaw();
}
