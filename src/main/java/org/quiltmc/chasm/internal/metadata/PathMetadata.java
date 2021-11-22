package org.quiltmc.chasm.internal.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

public class PathMetadata implements Metadata {
    private final List<Object> entries;

    public PathMetadata() {
        this.entries = new ArrayList<>();
    }

    private PathMetadata(List<Object> entries) {
        this.entries = new ArrayList<>(entries);
    }

    @Override
    public PathMetadata copy() {
        return new PathMetadata(entries);
    }

    private PathMetadata append(Object entry) {
        List<Object> newIndices = new ArrayList<>(entries);
        newIndices.add(entry);
        return new PathMetadata(newIndices);
    }

    public PathMetadata append(String name) {
        return append((Object) name);
    }

    public PathMetadata append(int index) {
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

    public PathMetadata getParent() {
        List<Object> newEntries = new ArrayList<>(entries);
        newEntries.remove(newEntries.size() - 1);
        return new PathMetadata(newEntries);
    }

    public boolean contains(PathMetadata other) {
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
            if (entry instanceof Integer && current instanceof ListNode) {
                current = ((ListNode) current).get((Integer) entry);
            } else if (entry instanceof String && current instanceof MapNode) {
                current = ((MapNode) current).get((String) entry);
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
        PathMetadata path = (PathMetadata) o;
        return Objects.equals(entries, path.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }
}
