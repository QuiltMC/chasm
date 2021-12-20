package org.quiltmc.chasm.internal.metadata;

import java.util.ArrayList;
import java.util.Objects;

import org.quiltmc.chasm.api.metadata.COWWrapperMetadataProvider;
import org.quiltmc.chasm.api.metadata.CowWrapperMetadata;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

public class ListPathMetadata extends ArrayList<ListPathMetadata.Entry> implements PathMetadata {
    public ListPathMetadata() {
    }

    private ListPathMetadata(PathMetadata entries) {
        super(entries);
    }

    @Override
    public PathMetadata deepCopy() {
        return new ListPathMetadata(this);
    }

    @Override
    public Object shallowCopy() {
        return new ListPathMetadata(this);
    }

    private PathMetadata append(Entry entry) {
        ListPathMetadata path = new ListPathMetadata(this);
        path.add(entry);
        return path;
    }

    @Override
    public PathMetadata append(String name) {
        return append(new Entry(name));
    }

    @Override
    public PathMetadata append(int index) {
        return append(new Entry(index));
    }

    @Override
    public PathMetadata parent() {
        PathMetadata path = new ListPathMetadata(this);
        path.remove(path.size() - 1);
        return path;
    }

    @Override
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

    @Override
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

    public static final class Entry {
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

    @Override
    public CowWrapperMetadata<PathMetadata> asWrapper(COWWrapperMetadataProvider parent, boolean owned) {
        CowWrapperMetadata<PathMetadata> wrapper = new CowWrapperPathMetadata(parent, this, owned);
        wrapper.toOwned(owned);
        return wrapper;
    }
}
