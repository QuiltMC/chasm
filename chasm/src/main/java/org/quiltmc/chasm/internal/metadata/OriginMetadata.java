package org.quiltmc.chasm.internal.metadata;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.metadata.COWWrapperMetadataProvider;
import org.quiltmc.chasm.api.metadata.Metadata;

public class OriginMetadata implements Metadata {
    private final String transformerId;

    public OriginMetadata(Transformation origin) {
        this(origin.getParent().getId());
    }

    private OriginMetadata(String transformerId) {
        this.transformerId = transformerId;
    }

    @Override
    public OriginMetadata deepCopy() {
        // Absolutely no reason to copy a purely immutable object
        return this;
    }

    @Override
    public Object shallowCopy() {
        // Absolutely no reason to copy a purely immutable object
        return this;
    }

    @Override
    public CowWrapperOriginMetadata asWrapper(COWWrapperMetadataProvider parent, boolean owned) {
        return new CowWrapperOriginMetadata(parent, this, owned);
    }
}
