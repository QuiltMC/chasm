package org.quiltmc.chasm.internal.metadata;

import java.util.ArrayList;
import java.util.List;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

public class PathMetadata extends ArrayList<PathEntry> implements Metadata {
    public PathMetadata() {}

    public PathMetadata(List<PathEntry> entries) {
        super(entries);
    }

    private PathMetadata append(PathEntry pathEntry) {
        PathMetadata path = new PathMetadata(this);
        path.add(pathEntry);
        return path;
    }

    public PathMetadata append(String name) {
        return append(new PathEntry(name));
    }

    public PathMetadata append(int index) {
        return append(new PathEntry(index));
    }

    public PathMetadata parent() {
        PathMetadata path = new PathMetadata(this);
        path.remove(path.size() - 1);
        return path;
    }

    public boolean startsWith(PathMetadata other) {
        if (other.size() > size()) {
            return false;
        }

        for (int i = 0; i < other.size(); i++) {
            if (!get(i).equals(other.get(i))) {
                return false;
            }
        }

        return true;
    }

    public Node resolve(Node root) {
        Node current = root;
        for (PathEntry pathEntry : this) {
            if (pathEntry.isInteger() && current instanceof ListNode) {
                current = ((ListNode) current).get(pathEntry.asInteger());
            } else if (pathEntry.isString() && current instanceof MapNode) {
                current = ((MapNode) current).get(pathEntry.toString());
            } else {
                throw new UnsupportedOperationException("Can't apply path to given node.");
            }
        }

        return current;
    }

    @Override
    public String toString() {
        return String.join("/", stream().map(PathEntry::toString).toArray(String[]::new));
    }

    @Override
    public FrozenMetadata freeze() {
        return new FrozenPathMetadata(this);
    }
}
