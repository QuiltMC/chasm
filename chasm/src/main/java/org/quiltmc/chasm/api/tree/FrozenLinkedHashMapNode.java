package org.quiltmc.chasm.api.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 *
 */
public class FrozenLinkedHashMapNode implements FrozenMapNode {
    private final Map<String, Node> map;

    /**
     * Freeze a {@link LinkedHashMapNode} as a new {@link FrozenLinkedHashMapNode}
     */
    public FrozenLinkedHashMapNode(LinkedHashMapNode linkedMapNode) {
        List<Map.Entry<String, FrozenNode>> 
        map = new HashMap<>(linkedMapNode);
    }

    @Override
    public MetadataProvider getMetadata() { // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsKey(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsValue(Object arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<Entry<String, Node>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node get(Object arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isEmpty() { // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<String> keySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node put(String arg0, Node arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Node> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public Node remove(Object arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Collection<Node> values() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MapNode asImmutable() {
        // TODO Auto-generated method stub
        return null;
    }

}
