package org.quiltmc.chasm.internal.cow;

/**
 * Represents a deeply and shallowly copyable object.
 */
public interface Copyable {
    /**
     * Deeply copy this object.
     *
     * <p>All mutable objects accessible from this object must also be copied as part of the new deep copy of this
     * Object.
     * This means that immutable objects need not copy at all, and may simply return {@code this}.
     *
     * @return The new deep copy of this object.
     */
    Object deepCopy();

    /**
     * Shallow copy this object.
     *
     * <p>Shallow copies must return an object that may be written to without reflecting the writes in the original
     * object.
     * This may be from returning {@code this} if this object is immutable, or from returning a new object with the same
     * objects accessible from it as the original.
     *
     * <p>Shallow copies must not cause recursive copying, but may shallow copy objects accessible from this object that
     * are considered implementation details.
     *
     * @return The new shallow copy of this object.
     */
    Object shallowCopy();
}
