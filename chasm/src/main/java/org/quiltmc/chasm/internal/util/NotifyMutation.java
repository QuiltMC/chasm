package org.quiltmc.chasm.internal.util;

/**
 * Notify objects holding this object that this object or a child object has changed state.
 */
public interface NotifyMutation {
    void notifyMutated();
}
