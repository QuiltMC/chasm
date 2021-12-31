/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.tree.CowWrapperNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.cow.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.cow.UpdatableCowWrapper;

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
     * @param <K>
     * @param object
     * @param owned
     * @param parent
     * @param key
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
    public MetadataProvider setMetadata(MetadataProvider other, CowWrapperMetadataProvider container) {
        if (!container.wrapsObject(this)) {
            throw new IllegalArgumentException();
        }
        this.toOwned();
        MetadataProvider old = this.getMetadata();
        if (other instanceof CowWrapperMetadataProvider) {
            throw new IllegalArgumentException(
                    "Will not put the wrapped metadata provider in this wrapper's base node; Call #updateThisParent() instead.");
        }
        this.object.setMetadata(other, container);
        return old;
    }

    protected abstract void updateThisNode(Object objKey, CowWrapperNode cowChild, Node contents);

    @Override
    protected void updateThisWrapper(Object objKey, UpdatableCowWrapper cowChild, Object objectContents) {
        if (objKey == AbstractChildCowWrapper.SentinelKeys.METADATA) {
            updateMetadata(cowChild, objectContents);
        }
        this.updateThisNode(objKey, (CowWrapperNode) cowChild, (Node) objectContents);
    }

    private void updateMetadata(UpdatableCowWrapper cowChild, Object objectContents) {
        if (!(cowChild instanceof CowWrapperMetadataProvider) || !(objectContents instanceof MetadataProvider)) {
            throw new IllegalArgumentException("Wrong metadata update typing.");
        }
        CowWrapperMetadataProvider child = (CowWrapperMetadataProvider) cowChild;
        MetadataProvider childContents = (MetadataProvider) objectContents;
        MetadataProvider sourced = this.object.getMetadata();
        if (this.metadataProviderWrapperCache == null) {
            this.metadataProviderWrapperCache = child;
            if (childContents != sourced) {
                this.toOwned();
                sourced = this.object.getMetadata();
                this.object.setMetadata(childContents, child);
            }
        }
    }

}
