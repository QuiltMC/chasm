/**
 *
 */
package org.quiltmc.chasm.api.util;

import org.quiltmc.chasm.internal.util.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.util.Copyable;

/**
 * A non-generic interface for general Cow methods.
 * Mostly implemented by {@link AbstractChildCowWrapper}.
 */
public interface CowWrapper extends Copyable {
    /**
     * Returns whether this wrapper is owned.
     *
     * @return Whether this wrapper is in owned (read-write) mode.
     */
    boolean isOwned();

    /**
     * Switches this wrapper to shared (read-only) mode.
     *
     * @return Whether this wrapper's ownership state changed.
     */
    boolean toShared();

    /**
     * Switches this wrapper to owned (read-write) mode.
     * Will shallow copy the contained object if it's currently shared.
     *
     * @return Whether this wrapper's ownership state changed.
     */
    boolean toOwned();

    /**
     * Switches this wrapper to the passed state.
     *
     * @param owned The new ownership mode of this wrapper.
     *
     * @return Whether this wrapper's ownership state changed.
     */
    boolean toOwned(boolean owned);

    /**
     * Checks if this wrapper wraps the passed object.
     *
     * @implNote Currently uses reference equality.
     *
     * @param o The object to check this wrapper for.
     *
     * @return Whether this wrapper wraps the passed object.
     */
    boolean wrapsObject(Object o);

}
