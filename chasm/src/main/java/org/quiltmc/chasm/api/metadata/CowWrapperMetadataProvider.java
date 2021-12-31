/**
 *
 */
package org.quiltmc.chasm.api.metadata;

import java.util.Map;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.cow.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.cow.UpdatableCowWrapper;
import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;

/**
 *
 */
public class CowWrapperMetadataProvider extends
        AbstractChildCowWrapper<MetadataProvider, CowWrapperMetadataProvider, UpdatableCowWrapper>
        implements MetadataProvider {
    private Map<Class<? extends Metadata>, Metadata> metadataCache;

    /**
     * @param metadata
     * @param owned
     */
    public <N extends Node, U extends AbstractCowWrapperNode<N, U>> CowWrapperMetadataProvider(
            AbstractCowWrapperNode<N, U> parent,
            MetadataProvider metadata, boolean owned) {
        super(parent, AbstractChildCowWrapper.SentinelKeys.METADATA, metadata, owned);
        metadataCache = null;
    }

    /**
     * @param cowWrapperMetadataProvider
     */
    protected CowWrapperMetadataProvider(CowWrapperMetadataProvider cowWrapperMetadataProvider) {
        super(cowWrapperMetadataProvider);
        metadataCache = null;
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
            this.metadataCache.put(dataClass, wrapper);
        }
        return dataClass.cast(wrapper);
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

    @SuppressWarnings("unchecked")
    private <M extends Metadata> void replaceMetadata(Class<? extends Metadata> key, Object metadata) {
        this.put((Class<M>) key, (M) key.cast(metadata));
    }

    @Override
    protected void updateThisWrapper(Object objKey, UpdatableCowWrapper cow, Object contents) {
        if (!(cow instanceof Metadata)) {
            throw new ClassCastException("Invalid metadata provider child list: " + cow);
        }
        Class<?> classKey = (Class<?>) objKey;
        Class<? extends Metadata> key = classKey.asSubclass(Metadata.class);
        Metadata child = this.object.get(key);
        if (child != contents || this.isOwned() != cow.isOwned()) {
            if (child != contents) {
                this.replaceMetadata(key, contents);
            }
            this.toOwned(cow.isOwned());
        }
    }

}
