/**
 *
 */
package org.quiltmc.chasm.api.metadata;

import java.util.Map;

import org.quiltmc.chasm.api.tree.CowWrapperNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.util.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.util.UpdatableCowWrapper;

/**
 *
 */
public class COWWrapperMetadataProvider extends
        AbstractChildCowWrapper<MetadataProvider, COWWrapperMetadataProvider, UpdatableCowWrapper>
        implements MetadataProvider {
    private Map<Class<? extends Metadata>, CowWrapperMetadata<? extends Metadata>> metadataWrapperCache;

    /**
     * @param metadata
     * @param owned
     */
    public <N extends Node, U extends CowWrapperNode<N, U>> COWWrapperMetadataProvider(CowWrapperNode<N, U> parent,
            MetadataProvider metadata, boolean owned) {
        super(parent, AbstractChildCowWrapper.SentinelKeys.METADATA, metadata, owned);
    }

    /**
     * @param cowWrapperMetadataProvider
     */
    protected COWWrapperMetadataProvider(COWWrapperMetadataProvider cowWrapperMetadataProvider) {
        super(cowWrapperMetadataProvider);
    }

    @Override
    public <T extends Metadata> void put(Class<T> dataClass, T data) {
        this.toOwned();
        this.object.put(dataClass, data);
        this.metadataWrapperCache.put(dataClass, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Metadata> T get(Class<T> dataClass) {
        T metadata = this.object.get(dataClass);
        if (metadata == null) {
            return null;
        }
        CowWrapperMetadata<? extends Metadata> wrapper = this.metadataWrapperCache.get(dataClass);
        if (wrapper == null || !wrapper.wrapsObject(metadata) || wrapper.isOwned() == this.isOwned()) {
            wrapper = (CowWrapperMetadata<? extends Metadata>) metadata.asWrapper(this, dataClass, isOwned());
            this.metadataWrapperCache.put(dataClass, wrapper);
        }
        return dataClass.cast(wrapper);
    }

    @Override
    protected COWWrapperMetadataProvider castThis() {
        return this;
    }

    @Override
    public COWWrapperMetadataProvider deepCopy() {
        COWWrapperMetadataProvider copy = new COWWrapperMetadataProvider(this);
        copy.toShared();
        return copy;
    }

    @Override
    public COWWrapperMetadataProvider shallowCopy() {
        return new COWWrapperMetadataProvider(this);
    }

    @SuppressWarnings("unchecked")
    private <M extends Metadata> void replaceMetadata(Class<? extends Metadata> key, Object metadata) {
        this.put((Class<M>) key, (M) key.cast(metadata));
    }

    @Override
    protected <C> void updateThisWrapper(Object objKey, CowWrapper cow, C contents) {
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
