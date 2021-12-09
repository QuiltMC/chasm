/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import java.lang.ref.SoftReference;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.tree.FrozenLinkedHashMapNode;
import org.quiltmc.chasm.api.tree.FrozenMapNode;
import org.quiltmc.chasm.api.tree.FrozenNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

/**
 *
 */
public class FrozenLazyClassNode extends FrozenLinkedHashMapNode implements LazyClassMapNode {


    private SoftReference<FrozenMapNode> fullNodeRef;

    /**
     * @param fullNodeRef
     */
    public FrozenLazyClassNode(LazyClassNode lazyClass, SoftReference<MapNode> fullNode) {
        super(lazyClass);
        MapNode optionalMap = fullNode.get();
        FrozenMapNode optionalFrozenMap = optionalMap == null ? null : optionalMap.asImmutable();
        fullNodeRef = new SoftReference<>(optionalFrozenMap);
    }

    @Override
    public FrozenLazyClassNode asImmutable() {
        return this;
    }

    @Override
    public MapNode pollFullNode() {
        return fullNodeRef.get();
    }

    @Override
    public FrozenMapNode getFullNode() {
        FrozenMapNode currentFullNode = fullNodeRef.get();
        if (currentFullNode == null) {
            MetadataProvider thawed = getMetadata().thaw();
            MapNode mutableFullNode = LazyClassNode.makeFullNode(getClassReader(), thawed);
            currentFullNode = mutableFullNode.asImmutable();
            fullNodeRef = new SoftReference<>(currentFullNode);
        }
        return currentFullNode;
    }

    @Override
    public ClassReader getClassReader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Entry<String, Node>> getNonLazyEntrySet() {
        // TODO Auto-generated method stub
        return null;
    }

}
