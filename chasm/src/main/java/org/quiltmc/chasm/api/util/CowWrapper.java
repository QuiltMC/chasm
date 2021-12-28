/**
 *
 */
package org.quiltmc.chasm.api.util;

import org.quiltmc.chasm.internal.cow.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.cow.Copyable;

/**
 * A non-generic interface for general Cow methods.
 * Mostly implemented by {@link AbstractChildCowWrapper}.
 */
public interface CowWrapper extends Copyable {
    /**
     * Returns whether this list is owned.
     *
     * @return Whether this list is in owned (read-write) mode.
     */
    boolean isOwned();

    /**
     * Switches this list to shared (read-only) mode.
     *
     * @return Whether this list's ownership state changed.
     */
    boolean toShared();

    /**
     * Switches this list to owned (read-write) mode.
     * Will shallow copy the contained object if it's currently shared.
     *
     * @return Whether this list's ownership state changed.
     */
    boolean toOwned();

    /**
     * Switches this list to the passed state.
     *
     * @param owned The new ownership mode of this list.
     *
     * @return Whether this list's ownership state changed.
     */
    boolean toOwned(boolean owned);

    /**
     * Checks if this list wraps the passed object.
     *
     * @implNote Currently uses reference equality.
     *
     * @param o The object to check this list for.
     *
     * @return Whether this list wraps the passed object.
     */
    boolean wrapsObject(Object o);

}
