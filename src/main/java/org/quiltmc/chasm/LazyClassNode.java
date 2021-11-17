package org.quiltmc.chasm;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.asm.ChasmClassVisitor;
import org.quiltmc.chasm.transformer.NodePath;
import org.quiltmc.chasm.tree.*;

import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.Set;

public class LazyClassNode extends AbstractMap<String, Node> implements MapNode {
    private final ClassReader classReader;
    private final MapNode nonLazyChildren;
    private final NodePath path;

    private SoftReference<MapNode> fullNode = new SoftReference<>(null);

    public LazyClassNode(ClassReader reader, NodePath path) {
        this.classReader = reader;
        this.path = path;

        // NOTE: Ensure parity with names in ChasmClassVisitor
        this.nonLazyChildren = new LinkedHashMapNode();
        this.nonLazyChildren.put(NodeConstants.ACCESS, new ValueNode<>(reader.getAccess()));
        this.nonLazyChildren.put(NodeConstants.NAME, new ValueNode<>(reader.getClassName()));
        this.nonLazyChildren.put(NodeConstants.SUPER, new ValueNode<>(reader.getSuperName()));

        ListNode interfaces = new LinkedListNode();
        for (String iface : reader.getInterfaces()) {
            interfaces.add(new ValueNode<>(iface));
        }
        this.nonLazyChildren.put(NodeConstants.INTERFACES, interfaces);
    }

    public MapNode getFullNode() {
        MapNode fullNode = this.fullNode.get();
        if (fullNode == null) {
            ChasmClassVisitor classVisitor = new ChasmClassVisitor();
            classReader.accept(classVisitor, ClassReader.SKIP_FRAMES);
            fullNode = classVisitor.getClassNode();
            if (getPath() != null) {
                fullNode.initializePath(getPath());
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

    @Override
    public void initializePath(NodePath path) {
        throw new UnsupportedOperationException("Can't set path on immutable node.");
    }

    @Override
    public NodePath getPath() {
        return path;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }
}
