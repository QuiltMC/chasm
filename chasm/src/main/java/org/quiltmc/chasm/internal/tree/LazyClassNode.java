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
public interface LazyClassNode extends MapNode {

    MapNode getFullNodeOrNull();

    MapNode getFullNode();

    ClassReader getClassReader();

    Set<Entry<String, Node>> getNonLazyEntrySet();

    @Override
    LazyClassNode deepCopy();

    @Override
    LazyClassNode shallowCopy();

    @Override
    <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperLazyClassNode asWrapper(
            AbstractCowWrapperNode<P, W> parent, Object key, boolean owned);
}
