/**
 *
 */
package org.quiltmc.chasm.internal;

import java.lang.ref.SoftReference;

import org.quiltmc.chasm.api.tree.FrozenLinkedHashMapNode;
import org.quiltmc.chasm.api.tree.FrozenMapNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

/**
 *
 */
public class FrozenLazyClassNode extends FrozenLinkedHashMapNode {

    private SoftReference<FrozenMapNode> fullNode;

    /**
     * @param fullNode
     */
    public FrozenLazyClassNode(LazyClassNode lazyClass, SoftReference<MapNode<Node>> fullNode) {
        super(lazyClass);
        MapNode<Node> optionalMap = fullNode.get();
        FrozenMapNode optionalFrozenMap = optionalMap == null ? null : optionalMap.asImmutable();
        this.fullNode = new SoftReference<>(optionalFrozenMap);
    }

}
