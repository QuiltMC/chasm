/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import org.quiltmc.chasm.api.tree.CowWrapperListNode;
import org.quiltmc.chasm.api.tree.CowWrapperNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.util.UpdatableCowWrapper;

/**
 *
 */
public class CowWrapperLazyClassNode extends CowWrapperMapNode {

    /**
     * @param <P>
     * @param <W>
     * @param parent
     * @param key
     * @param lazyClassMapNode
     * @param owned
     */
    public <K, P extends Node, W extends CowWrapperNode<P, W>> CowWrapperLazyClassNode(CowWrapperNode<P, W> parent,
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


}