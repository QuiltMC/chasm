/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

/**
 *
 */
public class CowWrapperLazyClassNode extends CowWrapperMapNode implements LazyClassNode {

    /**
     * @param <P>
     * @param <W>
     * @param parent
     * @param key
     * @param lazyClassMapNode
     * @param owned
     */
    public <K, P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperLazyClassNode(
            AbstractCowWrapperNode<P, W> parent,
            K key, LazyClassMapNode lazyClassMapNode,
            boolean owned) {
        super(parent, key, lazyClassMapNode, owned);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cowWrapperListNode
     */
    public CowWrapperLazyClassNode(CowWrapperMapNode cowWrapperListNode) {
        super(cowWrapperListNode);
        // TODO Auto-generated constructor stub
    }

    @Override
    public CowWrapperLazyClassNode deepCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CowWrapperLazyClassNode shallowCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MapNode getFullNodeOrNull() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MapNode getFullNode() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassReader getClassReader() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Entry<String, Node>> getNonLazyEntrySet() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperLazyClassNode asWrapper(
            AbstractCowWrapperNode<P, W> parent, Object key, boolean owned) {
        // TODO Auto-generated method stub
        return null;
    }

}
