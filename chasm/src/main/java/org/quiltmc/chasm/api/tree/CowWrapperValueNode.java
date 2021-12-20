/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.util.CowWrapper;

public class CowWrapperValueNode extends CowWrapperNode<ValueNode, CowWrapperValueNode> implements ValueNode {

    /**
     * @param parent
     * @param key
     * @param object
     * @param owned
     */
    protected <P extends Node, W extends CowWrapperNode<P, W>, K> CowWrapperValueNode(CowWrapperNode<P, W> parent,
            K key, ValueNode object, boolean owned) {
        super(parent, key, object, owned);
        // TODO Auto-generated constructor stub
    }

    @Override
    public Node shallowCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <P extends Node, W extends CowWrapperNode<P, W>> Node asWrapper(CowWrapperNode<P, W> parent, Object key,
            boolean owned) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getValue() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T getValueAs(Class<T> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getValueAsString() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getValueAsInt() { // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean getValueAsBoolean() { // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected CowWrapperValueNode castThis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CowWrapperValueNode deepCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <C> void updateThisWrapper(Object key, CowWrapper child, C contents) {
        // TODO Auto-generated method stub

    }


}
