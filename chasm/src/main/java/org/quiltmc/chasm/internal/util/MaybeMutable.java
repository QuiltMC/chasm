package org.quiltmc.chasm.internal.util;

/**
 * Methods to ensure an object is mutable or immutable, or obtain a copy that is either.
 */
public interface MaybeMutable extends NotifyMutation {
    /**
     * Checks whether the current object is mutable.
     *
     * @return {@code true} if this object is mutable, and {@code false} otherwise.
     */
    boolean isMutable();

    /**
     * Checks whether the current object is immutable.
     *
     * @return {@code true} if this object is immutable, and {@code false} otherwise.
     *
     * @implNote By default calls and logically negates {@link MaybeMutable#isMutable}.
     */
    default boolean isImmutable() { return !isMutable(); }

    /**
     * Returns a mutable version of this object.
     *
     * @return This object as a mutable object, copying if necessary.
     */
    default MaybeMutable asMutable() {
        if (isMutable()) {
            return this;
        }
        return asMutableCopy();
    }

    /**
     * Returns an immutable version of this object.
     *
     * <p>May be this object itself if this object is immutable.
     *
     * @return An immutable version of this object, or {@code this}.
     *
     * @apiNote A method called {@code asImmutableCopy} would be expected in this interface,
     *              but there's no reason to identically copy immutable objects.
     */
    MaybeMutable asImmutable();

    /**
     * Returns a mutable copy of this object.
     *
     * @return A mutable copy of this object. Must not return {@code this}.
     *
     * @apiNote This method exists because some callers may wish to not share mutable state.
     */
    MaybeMutable asMutableCopy();
}
