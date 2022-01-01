/**
 *
 */
package org.quiltmc.chasm.internal.cow;

import org.quiltmc.chasm.api.util.CowWrapper;

/**
 * Allows updating parent Cow Wrappers upon copy, and restrictive access to the parent link.
 */
public interface UpdatableCowWrapper extends CowWrapper, ChildCowWrapper {
    boolean checkKey(Object key);
}
