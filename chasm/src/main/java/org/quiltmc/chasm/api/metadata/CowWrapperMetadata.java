
package org.quiltmc.chasm.api.metadata;

import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.cow.AbstractChildCowWrapper;

/**
 * A read-write locking lazy {@link CowWrapper} for {@link Metadata}.
 *
 * @param <T> The type of metadata wrapped by this metadata wrapper.
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
    public abstract <M extends Metadata> M asWrapper(CowWrapperMetadataProvider parent, Class<M> key, boolean owned);

    @Override
    protected final CowWrapper getCachedCowWrapper(Object key) {
        return null;
    }

    @Override
    protected final CowWrapper setCachedCowWrapper(Object key, CowWrapper wrapper) {
        return throwUnsupportedOperation();
    }

    @Override
    protected final boolean clearCachedCowWrappers() {
        return false;
    }

    @Override
    protected final Object getChildObject(Object key) {
        return throwUnsupportedOperation();
    }

    @Override
    protected final Object setChildObject(Object key, Object value) {
        return throwUnsupportedOperation();
    }

    private static final <T> T throwUnsupportedOperation() {
        throw new UnsupportedOperationException();
    }
}
