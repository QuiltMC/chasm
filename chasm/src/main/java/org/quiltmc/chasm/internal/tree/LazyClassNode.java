package org.quiltmc.chasm.internal.tree;

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.api.util.ClassInfoProvider;
import org.quiltmc.chasm.internal.asm.visitor.ChasmClassVisitor;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.PathInitializer;

public class LazyClassNode extends AbstractMap<String, Node> implements MapNode {
    private final ClassReader classReader;
    private final ClassInfoProvider classInfoProvider;
    private final MapNode nonLazyChildren;
    private final MetadataProvider metadataProvider;
    private SoftReference<MapNode> fullNode = new SoftReference<>(null);

    public LazyClassNode(ClassReader reader, ClassInfoProvider classInfoProvider, MetadataProvider metadataProvider) {
        this.classReader = reader;
        this.classInfoProvider = classInfoProvider;
        this.metadataProvider = metadataProvider;

        // NOTE: Ensure parity with names in ChasmClassVisitor
        this.nonLazyChildren = new LinkedHashMapNode();
        this.nonLazyChildren.put(NodeConstants.ACCESS, new ValueNode(reader.getAccess()));
        this.nonLazyChildren.put(NodeConstants.NAME, new ValueNode(reader.getClassName()));
        this.nonLazyChildren.put(NodeConstants.SUPER, new ValueNode(reader.getSuperName()));

        ListNode interfaces = new ArrayListNode();
        for (String iface : reader.getInterfaces()) {
            interfaces.add(new ValueNode(iface));
        }
        this.nonLazyChildren.put(NodeConstants.INTERFACES, interfaces);
    }

    @Override
    public MapNode copy() {
        LazyClassNode copy = new LazyClassNode(classReader, classInfoProvider, metadataProvider.copy());

        for (Entry<String, Node> entry : nonLazyChildren.entrySet()) {
            copy.nonLazyChildren.put(entry.getKey(), entry.getValue().copy());
        }

        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }

    public MapNode getFullNodeOrNull() {
        return this.fullNode.get();
    }

    public MapNode getFullNode() {
        MapNode fullNode = this.fullNode.get();
        if (fullNode == null) {
            ChasmClassVisitor classVisitor = new ChasmClassVisitor(classInfoProvider);
            classReader.accept(classVisitor, 0);
            fullNode = classVisitor.getClassNode();

            fullNode.getMetadata().copyFrom(this.getMetadata());
            if (getMetadata().get(PathMetadata.class) != null) {
                PathInitializer.initialize(fullNode, getMetadata().get(PathMetadata.class));
            }

            this.fullNode = new SoftReference<>(fullNode);
        }

        return fullNode;
    }

    public ClassReader getClassReader() {
        return classReader;
    }

    @Override
    public Node get(Object key) {
        if (nonLazyChildren.containsKey(key)) {
            return nonLazyChildren.get(key);
        }

        return super.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        if (nonLazyChildren.containsKey(key)) {
            return true;
        }

        return super.containsKey(key);
    }

    @Override
    public Set<Entry<String, Node>> entrySet() {
        return getFullNode().entrySet();
    }

    public Set<Entry<String, Node>> getNonLazyEntrySet() {
        return nonLazyChildren.entrySet();
    }
}
