package org.quiltmc.chasm.internal;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.chasm.lang.api.metadata.Metadata;

/**
 * Represents the data of a class used for transforming.
 */
@ApiStatus.Internal
public final class ClassData {
    private final byte @NotNull [] classBytes;
    private final @NotNull Metadata metadata;

    public ClassData(byte @NotNull [] classBytes, @NotNull Metadata metadata) {
        this.classBytes = classBytes;
        this.metadata = metadata;
    }

    public byte @NotNull [] getClassBytes() {
        return classBytes;
    }

    public @NotNull Metadata getMetadata() {
        return metadata;
    }
}
