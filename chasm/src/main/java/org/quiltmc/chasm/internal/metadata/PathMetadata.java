package org.quiltmc.chasm.internal.metadata;

import java.util.ArrayList;
import java.util.Objects;

import org.quiltmc.chasm.api.metadata.Metadata;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

public class PathMetadata extends ArrayList<PathMetadata.Entry> implements Metadata {
    public PathMetadata() {
    }

    private PathMetadata(PathMetadata entries) {
        super(entries);
    }

    @Override
    public PathMetadata copy() {
        return new PathMetadata(this);
    }

    private PathMetadata append(Entry entry) {
        PathMetadata path = new PathMetadata(this);
        path.add(entry);
        return path;
    }

    public PathMetadata append(String name) {
        return append(new Entry(name));
    }

    public PathMetadata append(int index) {
        return append(new Entry(index));
    }

    public PathMetadata parent() {
        PathMetadata path = new PathMetadata(this);
        path.remove(path.size() - 1);
        return path;
    }

    public boolean startsWith(PathMetadata other) {
        if (other.size() > this.size()) {
            return false;
        }

        for (int i = 0; i < other.size(); i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }

    public Node resolve(Node root) {
        Node current = root;
        for (Entry entry : this) {
            if (entry.isInteger() && current instanceof ListNode) {
                current = Node.asList(current).get(entry.asInteger());
            } else if (entry.isString() && current instanceof MapNode) {
                current = Node.asMap(current).get(entry.asString());
            } else {
                throw new UnsupportedOperationException("Can't apply path to given node.");
            }
        }

        return current;
    }

    @Override
    public String toString() {
        return String.join("/", this.stream().map(e -> e.value.toString()).toArray(String[]::new));
    }

    public static class Entry {
        private final Object value;

        public Entry(int value) {
            this.value = value;
        }

        public Entry(String value) {
            this.value = value;
        }

        public boolean isInteger() {
            return value instanceof Integer;
        }

        public boolean isString() {
            return value instanceof String;
        }

        public int asInteger() {
            return (Integer) value;
        }

        public String asString() {
            return (String) value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Entry entry = (Entry) o;
            return Objects.equals(value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }
}
