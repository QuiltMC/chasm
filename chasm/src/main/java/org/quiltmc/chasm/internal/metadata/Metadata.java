package org.quiltmc.chasm.internal.metadata;

public interface Metadata {
    FrozenMetadata freeze();

    default Metadata thaw() {
        return this;
    }
}
