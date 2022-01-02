/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.tree.CowWrapperNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.cow.AbstractChildCowWrapper;
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
     * @param key
     */
    protected AbstractCowWrapperNode(UpdatableCowWrapperNode parent, Object key, N object,
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
        if (other instanceof CowWrapperMetadataProvider) {
            throw new IllegalArgumentException(
                    "Will not put the wrapped metadata provider in this wrapper's base node; Call #updateThisParent() instead.");
        }
        this.toOwned();
        MetadataProvider old = this.getMetadata();
        this.object.setMetadata(other, container);
        return old;
    }

    @Override
    protected final CowWrapper getCachedCowWrapper(Object key) {
        if (key == AbstractChildCowWrapper.SentinelKeys.METADATA) {
            return this.metadataProviderWrapperCache;
        }
        return getCachedCowWrapperNode(key);
    }


    @Override
    protected final CowWrapper setCachedCowWrapper(Object key, CowWrapper wrapper) {
        if (key == AbstractChildCowWrapper.SentinelKeys.METADATA) {
            CowWrapperMetadataProvider old = this.metadataProviderWrapperCache;
            this.metadataProviderWrapperCache = (CowWrapperMetadataProvider) wrapper;
            return old;
        }
        return setCachedCowWrapperNode((String) key, (CowWrapperNode) wrapper);
    }

    @Override
    protected final boolean clearCachedCowWrappers() {
        boolean removed = false;
        if (this.metadataProviderWrapperCache != null) {
            this.metadataProviderWrapperCache.unlinkParentWrapper();
            this.metadataProviderWrapperCache = null;
            removed = true;
        }
        final boolean removedNodeWrappers = this.clearCachedCowWrapperNodes();
        return removed || removedNodeWrappers;
    }

    @Override
    protected final Object getChildObject(Object key) {
        if (key == AbstractChildCowWrapper.SentinelKeys.METADATA) {
            return this.object.getMetadata();
        }
        return this.getChildNode(key);
    }


    @Override
    protected final Object setChildObject(Object key, Object value) {
        if (key == AbstractChildCowWrapper.SentinelKeys.METADATA) {
            CowWrapperMetadataProvider container = this.metadataProviderWrapperCache;
            MetadataProvider provider = (MetadataProvider) value;
            if (container == null || !container.wrapsObject(provider)) {
                container = new CowWrapperMetadataProvider(this, provider, this.isOwned());
                this.metadataProviderWrapperCache = container;
            }
            return this.object.setMetadata(provider, container);
        }
        return this.setChildNode(key, (Node) value);
    }

    /**
     * Gets the cached {@link CowWrapperNode} with the corresponding key.
     *
     * @param key The Object corresponding to the {@code CowNodeWrapper} to get.
     *
     * @return The cached {@code CowNodeWrapper} corresponding to the given key.
     */
    protected abstract CowWrapperNode getCachedCowWrapperNode(Object key);

    /**
     * Sets the cached {@link CowWrapperNode} with with the given key.
     *
     * @param key The Object key of the {@code CowWrapperNode} to replace.
     * @param wrapper The wrapper to put in the cache at the given key.
     *
     * @return The old cached {@code CowWrapperNode}.
     */
    protected abstract CowWrapper setCachedCowWrapperNode(Object key, CowWrapperNode wrapper);

    /**
     * Removes all {@link CowWrapperNode}s cached in this cow wrapper node.
     *
     * @return Whether any cow wrapper nodes were removed.
     */
    protected abstract boolean clearCachedCowWrapperNodes();

    /**
     * Gets the child {@link Node} of this wrapper's contained object corresponding to the given key.
     *
     * @param key The key of the child node to return.
     *
     * @return The child node with the given key.
     */
    protected abstract Node getChildNode(Object key);

    /**
     * Sets the contained object's child {@link Node} at the given key.
     *
     * @param key Where to set the child {@code Node}.
     * @param value The new value for the contained object's child {@code Node} with the corresponding key.
     *
     * @return The old {@code Node} with the passed key.
     */
    protected abstract Node setChildNode(Object key, Node value);
}
