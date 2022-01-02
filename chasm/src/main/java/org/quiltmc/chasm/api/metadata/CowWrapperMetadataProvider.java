/**
 *
 */
package org.quiltmc.chasm.api.metadata;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.cow.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.cow.AbstractCowWrapper;
import org.quiltmc.chasm.internal.cow.UpdatableCowWrapper;
import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;

/**
 *
 */
public class CowWrapperMetadataProvider extends
        AbstractChildCowWrapper<MetadataProvider, CowWrapperMetadataProvider, UpdatableCowWrapper>
        implements MetadataProvider {
    private Map<Class<? extends Metadata>, Metadata> metadataCache = null;

    /**
     * @param metadata
     * @param owned
     */
    public <N extends Node, U extends AbstractCowWrapperNode<N, U>> CowWrapperMetadataProvider(
            AbstractCowWrapperNode<N, U> parent,
            MetadataProvider metadata, boolean owned) {
        super(parent, AbstractChildCowWrapper.SentinelKeys.METADATA, metadata, owned);
    }

    /**
     * @param cowWrapperMetadataProvider
     */
    protected CowWrapperMetadataProvider(CowWrapperMetadataProvider cowWrapperMetadataProvider) {
        super(cowWrapperMetadataProvider);
    }

    public <T extends Metadata> T getCachedWrapper(Class<T> dataClass) {
        if (this.metadataCache == null) {
            return null;
        }
        return dataClass.cast(this.metadataCache.get(dataClass));
    }

    public <T extends Metadata> T removeCachedWrapper(Class<T> dataClass) {
        if (this.metadataCache == null) {
            return null;
        }
        T cached = dataClass.cast(this.metadataCache.remove(dataClass));
        if (cached instanceof CowWrapperMetadata<?>) {
            CowWrapperMetadata<?> wrapper = (CowWrapperMetadata<?>) cached;
            wrapper.unlinkParentWrapper();
        }
        return cached;
    }

    @Override
    public <T extends Metadata> void put(Class<T> dataClass, T data) {
        this.toOwned();
        this.removeCachedWrapper(dataClass);
        this.object.put(dataClass, data);
    }

    @Override
    public <T extends Metadata> T get(Class<T> dataClass) {
        T wrapper = this.getCachedWrapper(dataClass);
        if (wrapper == null) {
            T metadata = this.object.get(dataClass);
            wrapper = metadata.asWrapper(this, dataClass, isOwned());
            putCachedWrapper(dataClass, wrapper);
        }
        return dataClass.cast(wrapper);
    }

    /**
     * @param dataClass
     * @param wrapper
     */
    private <T extends Metadata> T putCachedWrapper(Class<T> dataClass, T wrapper) {
        if (this.metadataCache == null) {
            this.metadataCache = new HashMap<>();
        }
        return dataClass.cast(this.metadataCache.put(dataClass, wrapper));
    }

    @Override
    protected CowWrapperMetadataProvider castThis() {
        return this;
    }

    @Override
    public CowWrapperMetadataProvider deepCopy() {
        CowWrapperMetadataProvider copy = new CowWrapperMetadataProvider(this);
        copy.toShared();
        copy.toOwned(this.isOwned());
        return copy;
    }

    @Override
    public CowWrapperMetadataProvider shallowCopy() {
        return new CowWrapperMetadataProvider(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected CowWrapperMetadata<? extends Metadata> getCachedCowWrapper(Object objKey) {
        if (this.metadataCache == null) {
            return null;
        }
        Class<? extends Metadata> key = castKey(objKey);
        return (CowWrapperMetadata<? extends Metadata>) this.metadataCache.get(key);
    }

    private static final Class<? extends Metadata> castKey(Object objKey) throws ClassCastException {
        Class<?> classKey = (Class<?>) objKey;
        Class<? extends Metadata> key = classKey.asSubclass(Metadata.class);
        return key;
    }

    @Override
    protected CowWrapper setCachedCowWrapper(Object key, CowWrapper wrapper) {
        if (this.metadataCache == null) {
            this.metadataCache = new HashMap<>();
        }
        return this.setCachedCowWrapper(castKey(key), wrapper);
    }

    @Override
    protected boolean clearCachedCowWrappers() {
        if (this.metadataCache == null || this.metadataCache.isEmpty()) {
            return false;
        }
        for (Metadata data : this.metadataCache.values()) {
            AbstractCowWrapper.clearCowWrapperParent((CowWrapper) data);
        }
        this.metadataCache.clear();
        return true;
    }

    @Override
    protected Metadata getChildObject(Object key) {
        return this.object.get(castKey(key));
    }

    @SuppressWarnings("unchecked")
    private <T extends Metadata> T unsafeSetChildMetadata(Class<? extends Metadata> key, Metadata value) {
        Metadata old = this.object.get(key);
        this.object.put((Class<T>) key, (T) key.cast(value));
        return (T) key.cast(old);
    }

    @Override
    protected Metadata setChildObject(Object key, Object value) {
        return unsafeSetChildMetadata(castKey(key), (Metadata) value);
    }

}
