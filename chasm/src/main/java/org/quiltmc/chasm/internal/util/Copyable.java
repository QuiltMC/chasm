package org.quiltmc.chasm.internal.util;

/**
 * Represents a deeply and shallowly copyable object.
 */
public interface Copyable {
    /**
     * Deeply copy this object.
     *
     * @return The new deep copy of this object.
     */
    Object deepCopy();

    /**
     * Shallow copy this object.
     *
     * @return The new shallow copy of this object.
     */
    Object shallowCopy();
}
