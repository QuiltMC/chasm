/**
 * 
 */
package org.quiltmc.chasm.internal.metadata;

import java.util.Objects;

public class PathEntry {
    private final Object value;

    public PathEntry(int value) {
        this.value = value;
    }

    public PathEntry(String value) {
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
        PathEntry pathEntry = (PathEntry) o;
        return Objects.equals(value, pathEntry.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}