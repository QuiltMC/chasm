package org.quiltmc.chasm.api.metadata;

import org.quiltmc.chasm.internal.cow.Copyable;

/**
 * {@link org.quiltmc.chasm.api.tree.Node} metadata, capable of being attached to a {@link MapMetadataProvider}.
 */
public interface Metadata extends Copyable {
    /**
     * Creates a deep copy of this {@link Metadata}.
     *
     * @return A deep copy of this instance.
     */
    @Override
    Metadata deepCopy();

    /**
     * @param <T>
     * @param parent
     * @param key
     * @param owned
     *
     * @return
     */
    <T extends Metadata> T asWrapper(CowWrapperMetadataProvider parent, Class<T> key, boolean owned);
}
