/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.util.AbstractChildCowWrapper;
import java.util.ConcurrentModificationException;

import org.quiltmc.chasm.api.metadata.CowWrapperMetadataProvider;

/**
 *
 */
public abstract class AbstractCowWrapperNode<N extends Node, W extends AbstractCowWrapperNode<N, W>>
        extends AbstractChildCowWrapper<N, W, UpdatableCowWrapperNode>
        implements UpdatableCowWrapperNode {
    private CowWrapperMetadataProvider metadataProviderWrapperCache;

    /**
     * @param object
     * @param owned
     * @param parent
     */
    protected <K extends Object> AbstractCowWrapperNode(UpdatableCowWrapperNode parent, K key, N object,
            boolean owned) {
        super(parent, key, object, owned);
    }

    protected AbstractCowWrapperNode(AbstractCowWrapperNode<N, W> cowWrapperNode) {
        super(cowWrapperNode);
    }

    @Override
    public MetadataProvider getMetadata() {
        if (this.metadataProviderWrapperCache == null) {
            this.metadataProviderWrapperCache = new CowWrapperMetadataProvider(this, this.object.getMetadata(),
                    this.isOwned());
        } else if (this.isOwned() != this.metadataProviderWrapperCache.isOwned()) {
            throw new ConcurrentModificationException();
        }
        return this.metadataProviderWrapperCache;
    }

    @Override
    protected <C> void updateThisWrapper(Object objKey, CowWrapper cowChild, C cContents) {
        if (objKey != AbstractChildCowWrapper.SentinelKeys.METADATA || !(cowChild instanceof CowWrapperMetadataProvider)
                || !(cContents instanceof MetadataProvider)) {
            throw new IllegalArgumentException("Only call this to update metadata");
        }
        CowWrapperMetadataProvider child = (CowWrapperMetadataProvider) cowChild;
        MetadataProvider childContents = (MetadataProvider) cContents;
        MetadataProvider sourced = this.object.getMetadata();
        if (this.metadataProviderWrapperCache == null) {
            this.metadataProviderWrapperCache = child;
            if (childContents != sourced) {
                this.toOwned();
                this.object.getMetadata();
            }
        }
    }

}
