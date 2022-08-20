package org.quiltmc.chasm.api;

import org.quiltmc.chasm.lang.api.metadata.Metadata;

/**
 * Represents the data of a class used for transforming.
 */
public class ClassData {
    private final byte[] classBytes;
    private final Metadata metadata;

    public ClassData(byte[] classBytes, Metadata metadata) {
        this.classBytes = classBytes;
        this.metadata = metadata;
    }

    public ClassData(byte[] classBytes) {
        this(classBytes, new Metadata());
    }

    public byte[] getClassBytes() {
        return classBytes;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
