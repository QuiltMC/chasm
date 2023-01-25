package org.quiltmc.chasm.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.lang.api.metadata.Metadata;

/**
 * The result type used for {@link org.quiltmc.chasm.api.ChasmProcessor#process()}.
 */
public class ClassResult {
    private final byte @Nullable [] classBytes;

    private final @NotNull Metadata metadata;

    private final @NotNull Type type;

    /**
     * Create a new {@link ClassResult}.
     *
     * @param classBytes The bytes representing the class of this result,
     *     or {@code null} if {@code type = Type.Removed}.
     * @param metadata The metadata associated with the class of this result.
     * @param type The {@link Type} of this result.
     */
    public ClassResult(byte @Nullable [] classBytes, @NotNull Metadata metadata, @NotNull Type type) {
        this.classBytes = classBytes;
        this.metadata = metadata;
        this.type = type;
    }

    /**
     * Get the bytes representing the class of this result.
     * This will be {@code null} if and only if {@link #getType} is {@link Type#Removed}.
     *
     * @return The bytes representing the class of this result,
     *     or {@code null} if {@link #getType} is {@link Type#Removed}.
     */
    public byte @Nullable [] getClassBytes() {
        return classBytes;
    }

    /**
     * Get the {@link Metadata} associated with the class of this result.
     *
     * @return The {@link Metadata} of the class.
     */
    public @NotNull Metadata getMetadata() {
        return metadata;
    }

    /**
     * Get the {@link Type} of this result.
     *
     * @return The {@link Type} of this result.
     */
    public @NotNull Type getType() {
        return type;
    }

    /**
     * Represents the possible outcomes of {@link ClassResult}.
     */
    public enum Type {
        /**
         * Indicates that the class hasn't been modified during processing.
         * Note that this doesn't apply to {@link org.quiltmc.chasm.lang.api.metadata.Metadata}.
         */
        Unmodified,

        /**
         * Indicates that the class may have been modified during processing.
         */
        Modified,

        /**
         * Indicates that this class was created during processing.
         */
        Added,

        /**
         * Indicates that this class was deleted during processing.
         * This means that {@link #getClassBytes} will return null.
         */
        Removed
    }
}
