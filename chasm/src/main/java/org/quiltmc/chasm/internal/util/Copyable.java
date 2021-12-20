/**
 *
 */
package org.quiltmc.chasm.internal.util;

/**
 *
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
