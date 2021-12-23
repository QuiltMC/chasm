package org.quiltmc.chasm.internal.tree;

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.metadata.MapMetadataProvider;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.internal.asm.visitor.ChasmClassVisitor;
import org.quiltmc.chasm.internal.metadata.ListPathMetadata;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.PathInitializer;

public class LazyClassMapNode extends AbstractMap<String, Node> implements MapNode, LazyClassNode {
    private final ClassReader classReader;
    private final MapNode nonLazyChildren;
    private MetadataProvider metadataProvider = new MapMetadataProvider();
    private SoftReference<MapNode> fullNode = new SoftReference<>(null);

    public LazyClassMapNode(ClassReader reader) {
        this.classReader = reader;

        // NOTE: Ensure parity with names in ChasmClassVisitor
        this.nonLazyChildren = new LinkedHashMapNode();
        this.nonLazyChildren.put(NodeConstants.ACCESS, new WrapperValueNode(reader.getAccess()));
        this.nonLazyChildren.put(NodeConstants.NAME, new WrapperValueNode(reader.getClassName()));
        this.nonLazyChildren.put(NodeConstants.SUPER, new WrapperValueNode(reader.getSuperName()));

        ListNode interfaces = new ArrayListNode();
        for (String iface : reader.getInterfaces()) {
            interfaces.add(new WrapperValueNode(iface));
        }
        this.nonLazyChildren.put(NodeConstants.INTERFACES, interfaces);
    }

    private LazyClassMapNode(LazyClassMapNode lazyClassMapNode) {
        this.classReader = lazyClassMapNode.classReader;
        this.nonLazyChildren = lazyClassMapNode.nonLazyChildren;
        this.metadataProvider = lazyClassMapNode.metadataProvider;
        this.fullNode = lazyClassMapNode.fullNode;
    }

    @Override
    public LazyClassMapNode deepCopy() {
        LazyClassMapNode copy = new LazyClassMapNode(classReader);
        copy.metadataProvider = metadataProvider.deepCopy();

        for (Entry<String, Node> entry : nonLazyChildren.entrySet()) {
            copy.nonLazyChildren.put(entry.getKey(), entry.getValue().deepCopy());
        }

        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }

    @Override
    public MapNode getFullNodeOrNull() {
        return this.fullNode.get();
    }

    @Override
    public MapNode getFullNode() {
        MapNode fullNode = this.fullNode.get();
        if (fullNode == null) {
            ChasmClassVisitor classVisitor = new ChasmClassVisitor();
            classReader.accept(classVisitor, 0);
            fullNode = classVisitor.getClassNode();

            if (getMetadata().get(ListPathMetadata.class) != null) {
                PathInitializer.initialize(fullNode, getMetadata().get(ListPathMetadata.class));
            }

            this.fullNode = new SoftReference<>(fullNode);
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

    @Override
    public Set<Entry<String, Node>> getNonLazyEntrySet() {
        return nonLazyChildren.entrySet();
    }

    @Override
    public LazyClassNode shallowCopy() {
        // TODO: is this correct?
        return new LazyClassMapNode(this);
    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> Node asWrapper(AbstractCowWrapperNode<P, W> parent, Object key,
            boolean owned) {
        return new CowWrapperLazyClassNode(parent, key, this, owned);
    }
}
