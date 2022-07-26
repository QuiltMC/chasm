package org.quiltmc.chasm.internal.metadata;

import java.util.Iterator;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.chasm.api.metadata.Metadata;

public class PathMetadata implements Metadata, Iterable<PathMetadata.Entry> {
    private final PathMetadata parent;
    private final Entry entry;

    public PathMetadata(PathMetadata parent, String entry) {
        this.parent = parent;
        this.entry = new Entry(entry);
    }

    public PathMetadata(PathMetadata parent, int entry) {
        this.parent = parent;
        this.entry = new Entry(entry);
    }

    public PathMetadata getParent() {
        return parent;
    }

    public Entry getEntry() {
        return entry;
    }

    public Entry getEntry(int index) {
        if (index >= getSize()) {
            throw new RuntimeException("Index out of bounds: " + index);
        }

        PathMetadata current = this;
        while (current.getSize() > index + 1) {
            current = current.parent;
        }

        return current.getEntry();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PathMetadata)) {
            return false;
        }
        PathMetadata other = (PathMetadata) o;
        return Objects.equals(entry, other.entry) && Objects.equals(parent, other.parent);
    }

    public boolean startsWith(PathMetadata other) {
        if (other.getSize() > getSize()) {
            return false;
        }

        PathMetadata current = this;
        while (current.getSize() > other.getSize()) {
            current = current.parent;
        }

        return current.equals(other);
    }

    public int getSize() {
        if (parent == null) {
            return 1;
        }

        return parent.getSize() + 1;
    }

    @Override
    public String toString() {
        if (parent == null) {
            return entry.asString();
        }

        return parent + "/" + entry;
    }

    @NotNull
    @Override
    public Iterator<Entry> iterator() {
        return new Iterator<Entry>() {
            Iterator<Entry> parentIter = parent == null ? null : parent.iterator();
            boolean isConsumed = false;

            @Override
            public boolean hasNext() {
                return parentIter != null || !isConsumed;
            }

            @Override
            public Entry next() {
                Entry next;
                if (parentIter != null) {
                    next = parentIter.next();
                    parentIter = null;
                } else if (!isConsumed) {
                    next = entry;
                    isConsumed = true;
                } else {
                    throw new RuntimeException();
                }

                return next;
            }
        };
    }

    public static class Entry {
        private Object value;

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

        public void set(String value) {
            this.value = value;
        }

        public void set(int value) {
            this.value = value;
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
