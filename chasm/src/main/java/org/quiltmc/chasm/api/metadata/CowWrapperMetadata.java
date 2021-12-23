/**
 *
 */
package org.quiltmc.chasm.api.metadata;

import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.util.AbstractChildCowWrapper;

/**
 * @param <T>
 *
 */
public abstract class CowWrapperMetadata<T extends Metadata>
        extends AbstractChildCowWrapper<T, CowWrapperMetadata<T>, CowWrapperMetadataProvider>
        implements Metadata {

    /**
     * @param object
     * @param owned
     */
    protected <M extends Metadata> CowWrapperMetadata(CowWrapperMetadataProvider parent, Class<M> key, T object,
            boolean owned) {
        super(parent, key, object, owned);
    }

    /**
     * @param other
     */
    protected CowWrapperMetadata(CowWrapperMetadata<T> other) {
        super(other);
    }

    @Override
    protected CowWrapperMetadata<T> castThis() {
        return this;
    }

    @Override
    public abstract CowWrapperMetadata<T> deepCopy();

    @Override
    protected <C> void updateThisWrapper(Object key, CowWrapper child, C contents) {
        throw new UnsupportedOperationException("Metadata has no children.");
    }

    @Override
    public abstract <T extends Metadata> T asWrapper(CowWrapperMetadataProvider parent, Class<T> key, boolean owned);

}
