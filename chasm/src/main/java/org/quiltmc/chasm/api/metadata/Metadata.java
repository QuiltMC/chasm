package org.quiltmc.chasm.api.metadata;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.util.MaybeMutable;

/**
 * {@link Node} metadata, capable of being attached to a {@link MetadataProvider}.
 */
public interface Metadata extends MaybeMutable {
    @Override
    default Metadata asMutable() {
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
    @Override
    Metadata asImmutable();

    /**
     * Returns a mutable copy of this object.
     *
     * @return A mutable copy of this object. Must not return {@code this}.
     *
     * @apiNote This method exists because some callers may wish to not share mutable state.
     */
    @Override
    Metadata asMutableCopy();
}
