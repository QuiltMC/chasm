/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.util.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.util.UpdatableCowWrapper;

import java.util.ConcurrentModificationException;

import org.quiltmc.chasm.api.metadata.COWWrapperMetadataProvider;

/**
 *
 */
public abstract class CowWrapperNode<N extends Node, W extends CowWrapperNode<N, W>>
        extends AbstractChildCowWrapper<N, W, UpdatableCowWrapper>
        implements Node {
    private COWWrapperMetadataProvider metadataProviderWrapperCache;

    /**
     * @param object
     * @param owned
     * @param parent
     */
    protected <K extends Object> CowWrapperNode(UpdatableCowWrapper parent, K key, N object, boolean owned) {
        super(parent, key, object, owned);
    }

    protected CowWrapperNode(CowWrapperNode<N, W> cowWrapperNode) {
        super(cowWrapperNode);
    }

    @Override
    public MetadataProvider getMetadata() {
        if (this.metadataProviderWrapperCache == null) {
            this.metadataProviderWrapperCache = new COWWrapperMetadataProvider(this, this.object.getMetadata(),
                    this.isOwned());
        } else if (this.isOwned() != this.metadataProviderWrapperCache.isOwned()) {
            throw new ConcurrentModificationException();
        }
        return this.metadataProviderWrapperCache;
    }

}
