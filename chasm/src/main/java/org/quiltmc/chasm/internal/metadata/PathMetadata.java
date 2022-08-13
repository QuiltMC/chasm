package org.quiltmc.chasm.internal.metadata;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public class PathMetadata implements Iterable<PathMetadata.Entry> {
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
            return entry.toString();
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
                return parentIter != null && parentIter.hasNext() || !isConsumed;
            }

            @Override
            public Entry next() {
                if (parentIter != null && parentIter.hasNext()) {
                    return parentIter.next();
                } else if (!isConsumed) {
                    isConsumed = true;
                    return entry;
                }

                throw new NoSuchElementException();
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
        public String toString() {
            return value.toString();
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
