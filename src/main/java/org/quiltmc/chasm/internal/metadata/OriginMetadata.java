package org.quiltmc.chasm.internal.metadata;

import org.quiltmc.chasm.api.Transformation;

public class OriginMetadata implements Metadata {
    private final String transformerId;

    public OriginMetadata(Transformation origin) {
        this(origin.getParent().getId());
    }

    private OriginMetadata(String transformerId) {
        this.transformerId = transformerId;
    }

    @Override
    public OriginMetadata copy() {
        return new OriginMetadata(transformerId);
    }
}
