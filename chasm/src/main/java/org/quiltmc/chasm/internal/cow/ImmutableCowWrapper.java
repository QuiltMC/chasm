/**
 *
 */
package org.quiltmc.chasm.internal.cow;

import org.quiltmc.chasm.api.util.CowWrapper;

/**
 *
 */
public interface ImmutableCowWrapper extends CowWrapper {

    @Override
    default Object deepCopy() {
        return this;
    }

    @Override
    default Object shallowCopy() {
        return this;
    }

    @Override
    default boolean isOwned() {
        return false;
    }

    @Override
    default boolean toShared() {
        return true;
    }

    @Override
    default boolean toOwned() {
        return false;
    }

    @Override
    default boolean toOwned(boolean owned) {
        return !owned;
    }

    @Override
    default boolean wrapsObject(Object o) {
        return o == this;
    }

}
