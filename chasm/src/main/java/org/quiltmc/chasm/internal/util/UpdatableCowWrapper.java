/**
 *
 */
package org.quiltmc.chasm.internal.util;

import org.quiltmc.chasm.api.util.CowWrapper;

public interface UpdatableCowWrapper extends CowWrapper {
    /**
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
