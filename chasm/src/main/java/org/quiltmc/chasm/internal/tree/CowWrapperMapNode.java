/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.quiltmc.chasm.api.tree.CowWrapperNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.util.UpdatableCowWrapper;

/**
 *
 */
public class CowWrapperMapNode extends CowWrapperNode<MapNode, CowWrapperMapNode> implements MapNode {

    /**
     * @param parent
     * @param key
     * @param object
     * @param owned
     */
    public <K> CowWrapperMapNode(UpdatableCowWrapper parent, K key, MapNode object, boolean owned) {
        super(parent, key, object, owned);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cowWrapperMapNode
     */
    public CowWrapperMapNode(CowWrapperMapNode cowWrapperMapNode) {
        super(cowWrapperMapNode);
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
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isEmpty() { // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Node get(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node put(String key, Node value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node remove(Object key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Node> m) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public Set<String> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Node> values() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Entry<String, Node>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CowWrapperMapNode castThis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CowWrapperMapNode deepCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <C> void updateThisWrapper(Object key, CowWrapper child, C contents) {
        // TODO Auto-generated method stub

    }


}
