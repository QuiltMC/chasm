/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;

public class CowWrapperValueNode extends AbstractCowWrapperNode<ValueNode, CowWrapperValueNode> implements ValueNode {

    /**
     * @param parent
     * @param key
     * @param object
     * @param owned
     */
    protected <P extends Node, W extends AbstractCowWrapperNode<P, W>, K> CowWrapperValueNode(
            AbstractCowWrapperNode<P, W> parent,
            K key, ValueNode object, boolean owned) {
        super(parent, key, object, owned);
    }

    public CowWrapperValueNode(CowWrapperValueNode other) {
        super(other);
    }

    @Override
    public CowWrapperValueNode shallowCopy() {
        return new CowWrapperValueNode(this);
    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperValueNode asWrapper(
            AbstractCowWrapperNode<P, W> parent, Object key, boolean owned) {
        CowWrapperValueNode copy = new CowWrapperValueNode(parent, key, object, owned);
        copy.toShared();
        copy.toOwned(owned);
        return copy;
    }

    @Override
    public Object getValue() { return this.object.getValue(); }

    @Override
    public <T> T getValueAs(Class<T> type) {
        return this.object.getValueAs(type);
    }

    @Override
    public String getValueAsString() { return this.object.getValueAsString();
    }

    @Override
    public int getValueAsInt() { return this.object.getValueAsInt();
    }

    @Override
    public boolean getValueAsBoolean() { return this.object.getValueAsBoolean();
    }

    @Override
    protected CowWrapperValueNode castThis() {
        return this;
    }

    @Override
    public CowWrapperValueNode deepCopy() {
        CowWrapperValueNode copy = new CowWrapperValueNode(this);
        copy.toShared();
        copy.toOwned(this.isOwned());
        return copy;
    }

    private static final <T> T throwUnsupportedOperation() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected CowWrapperNode getCachedCowWrapperNode(Object key) {
        return null;
    }

    @Override
    protected CowWrapper setCachedCowWrapperNode(Object key, CowWrapperNode wrapper) {
        return throwUnsupportedOperation();
    }

    @Override
    protected boolean clearCachedCowWrapperNodes() {
        return throwUnsupportedOperation();
    }

    @Override
    protected Node getChildNode(Object key) {
        return throwUnsupportedOperation();
    }

    @Override
    protected Node setChildNode(Object key, Node value) {
        return throwUnsupportedOperation();
    }


}