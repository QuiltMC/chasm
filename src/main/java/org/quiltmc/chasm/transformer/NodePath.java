package org.quiltmc.chasm.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;

public class NodePath {
    private final List<Object> entries;

    public NodePath() {
        this.entries = new ArrayList<>();
    }

    private NodePath(List<Object> entries) {
        this.entries = entries;
    }

    private NodePath append(Object entry) {
        List<Object> newIndices = new ArrayList<>(entries);
        newIndices.add(entry);
        return new NodePath(newIndices);
    }

    public NodePath append(String name) {
        return append((Object) name);
    }

    public NodePath append(int index) {
        return append((Integer) index);
    }

    public int getLength() {
        return entries.size();
    }

    public Object getLastEntry() {
        return entries.get(entries.size() - 1);
    }

    public Object getEntryAt(int index) {
        return entries.get(index);
    }

    public void setEntryAt(int index, Object value) {
        entries.set(index, value);
    }

    public NodePath getParent() {
        List<Object> newEntries = new ArrayList<>(entries);
        newEntries.remove(newEntries.size() - 1);
        return new NodePath(newEntries);
    }

    public boolean contains(NodePath other) {
        if (entries.size() > other.entries.size()) {
            return false;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i) != other.entries.get(i)) {
                return false;
            }
        }

        return true;
    }

    public Node resolve(Node root) {
        Node current = root;
        for (Object entry : entries) {
            if (entry instanceof Integer indexPathEntry && current instanceof ListNode listNode) {
                current = listNode.get(indexPathEntry);
            } else if (entry instanceof String namePathEntry && current instanceof MapNode mapNode) {
                current = mapNode.get(namePathEntry);
            } else {
                throw new UnsupportedOperationException("Can't apply path to given node.");
            }
        }

        return current;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodePath nodePath = (NodePath) o;
        return Objects.equals(entries, nodePath.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }
}
