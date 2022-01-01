/**
 * 
 */
package org.quiltmc.chasm.internal.cow;

/**
 * 
 */
public interface ChildCowWrapper {

    /**
     * Updates this wrapper as the parent of the passed wrapper
     *
     * <p>If the passed contents matches this wrapper's child object with the given key,
     * and
     *
     * @param key
     * @param child
     * @param contents
     *
     * @return
     */
    boolean updateWrapper(Object key, UpdatableCowWrapper child, Object contents);

    boolean unlinkParentWrapper();

    boolean checkParentLink(Object o);

}
