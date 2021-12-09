package org.quiltmc.chasm.internal.tree;

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.FrozenMapNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.asm.visitor.ChasmClassVisitor;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class LazyClassNode extends AbstractMap<String, Node> implements LazyClassMapNode {
    private final ClassReader classReader;
    private final MapNode nonLazyChildren;
    private MetadataProvider metadataProvider = new MetadataProvider();
    private SoftReference<MapNode> fullNodeRef = new SoftReference<>(null);

    public LazyClassNode(ClassReader reader) {
        classReader = reader;

        // NOTE: Ensure parity with names in ChasmClassVisitor
        nonLazyChildren = new LinkedHashMapNode();
        nonLazyChildren.put(NodeConstants.ACCESS, new ValueNode(reader.getAccess()));
        nonLazyChildren.put(NodeConstants.NAME, new ValueNode(reader.getClassName()));
        nonLazyChildren.put(NodeConstants.SUPER, new ValueNode(reader.getSuperName()));

        ListNode interfaces = new ArrayListNode();
        for (String iface : reader.getInterfaces()) {
            interfaces.add(new ValueNode(iface));
        }
        nonLazyChildren.put(NodeConstants.INTERFACES, interfaces);
    }

    @Override
    public FrozenMapNode asImmutable() {
        FrozenLazyClassNode frozen = new FrozenLazyClassNode(this, fullNodeRef);

        return frozen;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }

    @Override
    public MapNode pollFullNode() {
        return fullNodeRef.get();
    }

    @Override
    public MapNode getFullNode() {
        MapNode fullNode = fullNodeRef.get();
        if (fullNode == null) {
            fullNode = makeFullNode(classReader, metadataProvider);
            fullNodeRef = new SoftReference<>(fullNode);
        }

        return fullNode;
    }

    static MapNode makeFullNode(ClassReader classReader,
            MetadataProvider metadata) {

        ChasmClassVisitor classVisitor = new ChasmClassVisitor();
        classReader.accept(classVisitor, 0);
        MapNode fullNode = classVisitor.getClassNode();

        PathMetadata pathMetadata = metadata.get(PathMetadata.class);
        if (pathMetadata != null) {
            fullNode.updatePath(pathMetadata);
        }

        return fullNode;
    }

    @Override
    public ClassReader getClassReader() {
        return classReader;
    }

    @Override
    public Node get(Object key) {
        if (nonLazyChildren.containsKey(key)) {
            return nonLazyChildren.get(key);
        }

        return getFullNode().get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        if (nonLazyChildren.containsKey(key)) {
            return true;
        }

        return super.containsKey(key);
    }

    @Override
    public Set<Map.Entry<String, Node>> entrySet() {
        return getFullNode().entrySet();
    }

    @Override
    public Set<Map.Entry<String, Node>> getNonLazyEntrySet() {
        return nonLazyChildren.entrySet();
    }
}
