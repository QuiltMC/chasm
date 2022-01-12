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
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

/**
 *
 */
public class FrozenLazyClassNode extends FrozenLinkedHashMapNode implements LazyClassMapNode {
    private final ClassReader classReader;
    private final Set<Entry<String, Node>> eagerEntrySet;

    private SoftReference<FrozenMapNode> fullNodeRef;


    /**
     * @param fullNodeRef
     */
    public FrozenLazyClassNode(LazyClassNode lazyClass) {
        super(lazyClass);
        MapNode optionalMap = lazyClass.getFullNode();
        FrozenMapNode optionalFrozenMap = optionalMap == null ? null : optionalMap.asImmutable();
        fullNodeRef = optionalFrozenMap == null ? null : new SoftReference<>(optionalFrozenMap);
    }

    @Override
    public FrozenLazyClassNode asImmutable() {
        return this;
    }

    @Override
    public MapNode pollFullNode() {
        return fullNodeRef == null ? null : fullNodeRef.get();
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
        return classReader;
    }

    @Override
    public Set<Entry<String, Node>> getNonLazyEntrySet() {
        return eagerEntrySet;
    }

}
