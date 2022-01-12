package org.quiltmc.chasm.api.tree;

import java.util.ArrayList;
import java.util.WeakHashMap;

import org.quiltmc.chasm.api.metadata.MetadataProvider;


/**
 * Uses an {@code ArrayList} to implement a {@link ListNode}.
 */
public class ArrayListNode extends ArrayList<Node> implements ListNode {
    private MetadataProvider metadataProvider;

    private WeakHashMap<Node, Void> parents;


    public ArrayListNode() {
        super();
        metadataProvider = new MetadataProvider();
    }

    public ArrayListNode(ListNode listNode) {
        super(listNode.size());
        metadataProvider = listNode.getMetadata().thaw();
        for (Node node : listNode) {
            ArrayListNode.this.add(node.asMutable());
        }
    }

    @Override
    public FrozenArrayListNode asImmutable() {
        return new FrozenArrayListNode(this);
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }

    @Override
    public Node asMutableCopy() {
        return new ArrayListNode(this);
    }

    @Override
    public void notifyMutated() {
        if (parents != null) {
            for (Node obj : parents.keySet()) {
                obj.notifyMutated();
            }
        }
    }

    public void registerParent(Node parent) {
        if (parents == null) {
            parents = new WeakHashMap<>(4);
        }
        parents.put(parent, null);
    }

    public void unregisterParent(Node parent) {
        parents.remove(parent);
    }
}
