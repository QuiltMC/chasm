package org.quiltmc.chasm.internal.metadata;

import org.quiltmc.chasm.api.Transformation;

public class OriginMetadata implements FrozenMetadata {
    private final String transformerId;

    public OriginMetadata(Transformation origin) {
        this(origin.getParent().getId());
    }

    private OriginMetadata(String transformerId) {
        this.transformerId = transformerId;
    }

    public String transformerID() {
        return transformerId;
    }

    @Override
    public Metadata thaw() {
        // Can't be thawed
        return this;
    }
}
