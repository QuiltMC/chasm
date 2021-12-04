/**
 *
 */
package org.quiltmc.chasm.internal;

import java.lang.ref.SoftReference;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.tree.FrozenLinkedHashMapNode;
import org.quiltmc.chasm.api.tree.FrozenMapNode;
import org.quiltmc.chasm.api.tree.FrozenNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.Metadata;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 *
 */
public class FrozenLazyClassNode extends FrozenLinkedHashMapNode implements LazyClassMapNode<FrozenNode> {


    private SoftReference<FrozenMapNode> fullNodeRef;

    /**
     * @param fullNodeRef
     */
    public FrozenLazyClassNode(LazyClassNode lazyClass, SoftReference<MapNode<Node>> fullNode) {
        super(lazyClass);
        MapNode<Node> optionalMap = fullNode.get();
        FrozenMapNode optionalFrozenMap = optionalMap == null ? null : optionalMap.asImmutable();
        fullNodeRef = new SoftReference<>(optionalFrozenMap);
    }

    @Override
    public FrozenLazyClassNode asImmutable() {
        return this;
    }

    @Override
    public FrozenMapNode pollFullNode() {
        return fullNodeRef.get();
    }

    @Override
    public FrozenMapNode getFullNode() {
        FrozenMapNode currentFullNode = fullNodeRef.get();
        if (currentFullNode == null) {
            MetadataProvider<Metadata> thawed = getMetadata().thaw();
            MapNode<Node> mutableFullNode = LazyClassNode.makeFullNode(getClassReader(), thawed);
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
    public Set<Entry<String, FrozenNode>> getNonLazyEntrySet() {
        // TODO Auto-generated method stub
        return null;
    }

}
