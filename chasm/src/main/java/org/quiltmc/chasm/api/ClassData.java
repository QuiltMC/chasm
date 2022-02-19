package org.quiltmc.chasm.api;

import org.quiltmc.chasm.api.metadata.MetadataProvider;

/**
 * Represents the data of a class used for transforming.
 */
public class ClassData {
    private final byte[] classBytes;
    private final MetadataProvider metadataProvider;

    public ClassData(byte[] classBytes, MetadataProvider metadataProvider) {
        this.classBytes = classBytes;
        this.metadataProvider = metadataProvider;
    }

    public ClassData(byte[] classBytes) {
        this(classBytes, new MetadataProvider());
    }

    public byte[] getClassBytes() {
        return classBytes;
    }

    public MetadataProvider getMetadataProvider() {
        return metadataProvider;
    }
}
