package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.target.NodeTarget;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.LazyNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.OriginMetadata;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public class TransformationApplier {
    private final ListNode classes;
    private final List<Transformation> transformations;

    private final Map<PathMetadata, List<Target>> affectedTargets;

    public TransformationApplier(ListNode classes, List<Transformation> transformations) {
        this.classes = classes;
        this.transformations = transformations;

        this.affectedTargets = new HashMap<>();
    }

    private static PathMetadata getPath(Target target) {
        PathMetadata path = target.getTarget().getMetadata().get(PathMetadata.class);
        if (path == null) {
            throw new RuntimeException("Node in specified target is missing path information.");
        }
        return path;
    }

    private List<Target> getAffectedTargets(PathMetadata path) {
        List<Target> affectedTargets = new ArrayList<>();

        for (Transformation transformation : transformations) {
            List<Target> targets = new ArrayList<>(transformation.getSources().values());
            targets.add(transformation.getTarget());

            for (Target target : targets) {
                if (target instanceof SliceTarget && getPath(target).startsWith(path)) {
                    affectedTargets.add(target);
                }

                if (target instanceof NodeTarget && getPath(target).parent().startsWith(path)) {
                    affectedTargets.add(target);
                }
            }
        }

        return affectedTargets;
    }

    public void applyAll() {
        for (Transformation transformation : transformations) {
            applyTransformation(transformation);
        }
    }

    private void applyTransformation(Transformation transformation) {
        Node target = resolveNode(getPath(transformation.getTarget()), false);
        MapNode sources = resolveSources(transformation);

        // TODO: Replace copies with immutability
        Node replacement = transformation.apply(target.copy(), sources.copy()).copy();
        replacement.getMetadata().put(OriginMetadata.class, new OriginMetadata(transformation));

        replaceTarget(transformation.getTarget(), replacement);
    }

    private void replaceTarget(Target target, Node replacement) {
        if (target instanceof NodeTarget) {
            replaceNode((NodeTarget) target, replacement);
        } else if (target instanceof SliceTarget && replacement instanceof ListNode) {
            replaceSlice((SliceTarget) target, Node.asList(replacement));
        } else {
            throw new RuntimeException("Invalid replacement for target");
        }
    }

    private void replaceNode(NodeTarget nodeTarget, Node replacement) {
        // Resolve containing node
        PathMetadata targetPath = getPath(nodeTarget);
        Node parentNode = resolveNode(targetPath.parent(), true);

        // Get index into parent node
        PathMetadata.Entry entry = targetPath.get(targetPath.size() - 1);

        // Replace in list
        if (parentNode instanceof ListNode && entry.isInteger()) {
            ListNode parentList = Node.asList(parentNode);
            parentList.set(entry.asInteger(), replacement);
            return;
        }

        // Replace in map
        if (parentNode instanceof MapNode && entry.isString()) {
            MapNode parentList = Node.asMap(parentNode);
            parentList.put(entry.asString(), replacement);
            return;
        }

        throw new RuntimeException("Invalid index into node.");
    }

    private void replaceSlice(SliceTarget sliceTarget, ListNode replacement) {
        // Resolve containing node
        PathMetadata targetPath = getPath(sliceTarget);
        Node parentNode = resolveNode(targetPath, true);

        if (!(parentNode instanceof ListNode)) {
            throw new UnsupportedOperationException("Replacement for slice target must be a list node.");
        }

        ListNode parentList = Node.asList(parentNode);

        int change = parentList.size() - replacement.size();
        int start = sliceTarget.getStartIndex() / 2;
        int end = sliceTarget.getEndIndex() / 2;
        int length = end - start;

        // Move all slice indices affected by this
        List<Target> affectedTargets =
                this.affectedTargets.computeIfAbsent(targetPath, this::getAffectedTargets);
        for (Target target : affectedTargets) {
            if (target instanceof NodeTarget) {
                movePathIndex(getPath(target), targetPath.size(), end, change);
            }

            if (target instanceof SliceTarget) {
                if (getPath(target).equals(targetPath)) {
                    moveSliceIndex((SliceTarget) target, end, change);
                } else {
                    movePathIndex(getPath(target), targetPath.size(), end, change);
                }
            }
        }

        // Remove old entries
        for (int i = 0; i < length; i++) {
            parentList.remove(start);
        }

        // Insert new entries
        for (Node entry : replacement) {
            parentList.add(start, entry);
        }
    }

    private void movePathIndex(PathMetadata path, int pathIndex, int endIndex, int amount) {
        int originalIndex = path.get(pathIndex).asInteger();
        if (originalIndex >= endIndex) {
            path.set(pathIndex, new PathMetadata.Entry(originalIndex + amount));
        }
    }

    private void moveSliceIndex(SliceTarget target, int end, int change) {
        if (target.getStartIndex() / 2 >= end) {
            target.setStartIndex(target.getStartIndex() + 2 * change);
        }

        if (target.getEndIndex() / 2 >= end) {
            target.setEndIndex(target.getEndIndex() + 2 * change);
        }
    }

    private Node resolveNode(PathMetadata path, boolean resolveLazyNodes) {
        Node currentNode = classes;

        for (PathMetadata.Entry entry : path) {
            if (currentNode instanceof ListNode && entry.isInteger()) {
                // Get next node
                ListNode listNode = Node.asList(currentNode);
                int index = entry.asInteger();
                Node nextNode = listNode.get(index);

                // Resolve lazy nodes
                if (resolveLazyNodes && nextNode instanceof LazyNode) {
                    nextNode = ((LazyNode) nextNode).getFullNode();
                    listNode.set(index, nextNode);
                }

                currentNode = nextNode;
            } else if (currentNode instanceof MapNode && entry.isString()) {
                // Get next node
                MapNode mapNode = Node.asMap(currentNode);
                String key = entry.asString();
                Node nextNode = mapNode.get(key);

                // Resolve lazy nodes
                if (resolveLazyNodes && nextNode instanceof LazyNode) {
                    nextNode = ((LazyNode) nextNode).getFullNode();
                    mapNode.put(key, nextNode);
                }

                currentNode = nextNode;
            } else {
                throw new RuntimeException("Can't resolve path " + path);
            }
        }

        return currentNode;
    }

    private MapNode resolveSources(Transformation transformation) {
        MapNode resolvedSources = new LinkedHashMapNode();
        for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
            resolvedSources.put(source.getKey(), resolveNode(getPath(source.getValue()), false));
        }
        return resolvedSources;
    }
}
