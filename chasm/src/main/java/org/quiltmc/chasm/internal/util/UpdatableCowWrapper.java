/**
 *
 */
package org.quiltmc.chasm.internal.util;

import org.quiltmc.chasm.api.util.CowWrapper;

/**
 * Allows updating parent Cow Wrappers upon copy, and restrictive access to the parent link.
 */
public interface UpdatableCowWrapper extends CowWrapper {
    /**
     * Updates this wrapper as the parent of the passed wrapper
     *
     * <p>If the passed contents matches this wrapper's child object with the given key,
     * and
     *
     * @param <C>
     * @param key
     * @param child
     * @param contents
     *
     * @return
     */
    <C> boolean updateParentWrapper(Object key, CowWrapper child, C contents);

    boolean unlinkParentWrapper();

    boolean checkParentLink(Object o);

}
