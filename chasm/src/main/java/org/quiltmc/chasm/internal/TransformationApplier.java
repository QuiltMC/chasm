package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.target.NodeTarget;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.internal.metadata.MetadataCache;
import org.quiltmc.chasm.internal.metadata.OriginMetadata;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.tree.ClassNode;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class TransformationApplier {
    private final MetadataCache metadataCache;
    private final ListNode classes;
    private final List<Transformation> transformations;

    private final Map<PathMetadata, List<Target>> affectedTargets;

    public TransformationApplier(MetadataCache metadataCache, ListNode classes, List<Transformation> transformations) {
        this.metadataCache = metadataCache;
        this.classes = classes;
        this.transformations = transformations;

        this.affectedTargets = new HashMap<>();
    }

    private PathMetadata getPath(Target target) {
        PathMetadata path = metadataCache.get(target.getTarget()).get(PathMetadata.class);
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

                if (target instanceof NodeTarget && getPath(target).getParent().startsWith(path)) {
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
        Node replacement = transformation.apply(target, sources.getEntries());
        metadataCache.get(replacement).put(OriginMetadata.class, new OriginMetadata(transformation));

        replaceTarget(transformation.getTarget(), replacement);
    }

    private void replaceTarget(Target target, Node replacement) {
        if (target instanceof NodeTarget) {
            replaceNode((NodeTarget) target, replacement);
        } else if (target instanceof SliceTarget && replacement instanceof ListNode) {
            replaceSlice((SliceTarget) target, NodeUtils.asList(replacement));
        } else {
            throw new RuntimeException("Invalid replacement for target");
        }
    }

    private void replaceNode(NodeTarget nodeTarget, Node replacement) {
        // Resolve containing node
        PathMetadata targetPath = getPath(nodeTarget);
        Node parentNode = resolveNode(targetPath.getParent(), true);

        // Get index into parent node
        PathMetadata.Entry entry = targetPath.getEntry();

        // Replace in list
        if (parentNode instanceof ListNode && entry.isInteger()) {
            ListNode parentList = NodeUtils.asList(parentNode);
            parentList.getEntries().set(entry.asInteger(), replacement);
            return;
        }

        // Replace in map
        if (parentNode instanceof MapNode && entry.isString()) {
            MapNode parentList = NodeUtils.asMap(parentNode);
            parentList.getEntries().put(entry.asString(), replacement);
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

        ListNode parentList = NodeUtils.asList(parentNode);

        int start = sliceTarget.getStartIndex() / 2;
        int end = sliceTarget.getEndIndex() / 2;
        int length = end - start;
        int change = replacement.getEntries().size() - length;

        // Move all slice indices affected by this
        List<Target> affectedTargets =
                this.affectedTargets.computeIfAbsent(targetPath, this::getAffectedTargets);
        for (Target target : affectedTargets) {
            if (target instanceof NodeTarget) {
                movePathIndex(getPath(target), targetPath.getSize(), end, change);
            }

            if (target instanceof SliceTarget) {
                if (getPath(target).equals(targetPath)) {
                    moveSliceIndex((SliceTarget) target, end, change);
                } else {
                    movePathIndex(getPath(target), targetPath.getSize(), end, change);
                }
            }
        }

        // Remove old entries
        for (int i = 0; i < length; i++) {
            parentList.getEntries().remove(start);
        }

        // Insert new entries
        for (Node entry : replacement.getEntries()) {
            parentList.getEntries().add(start, entry);
        }
    }

    private void movePathIndex(PathMetadata path, int pathIndex, int endIndex, int amount) {
        PathMetadata.Entry entry = path.getEntry(pathIndex);
        int originalIndex = entry.asInteger();
        if (originalIndex >= endIndex) {
            entry.set(originalIndex + amount);
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
                ListNode listNode = NodeUtils.asList(currentNode);
                int index = entry.asInteger();
                Node nextNode = listNode.getEntries().get(index);

                // Resolve lazy nodes
                if (resolveLazyNodes && nextNode instanceof ClassNode) {
                    nextNode = new MapNode(((ClassNode) nextNode).getLazyEntries());
                    listNode.getEntries().set(index, nextNode);
                }

                currentNode = nextNode;
            } else if (currentNode instanceof MapNode && entry.isString()) {
                // Get next node
                MapNode mapNode = NodeUtils.asMap(currentNode);
                String key = entry.asString();
                Node nextNode = mapNode.getEntries().get(key);

                // Resolve lazy nodes
                if (resolveLazyNodes && nextNode instanceof ClassNode) {
                    nextNode = new MapNode(((ClassNode) nextNode).getLazyEntries());
                    mapNode.getEntries().put(key, nextNode);
                }

                currentNode = nextNode;
            } else {
                throw new RuntimeException("Can't resolve path " + path);
            }
        }

        Objects.requireNonNull(currentNode);
        return currentNode;
    }

    private MapNode resolveSources(Transformation transformation) {
        MapNode resolvedSources = new MapNode(new LinkedHashMap<>());
        for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
            resolvedSources.getEntries().put(source.getKey(), resolveNode(getPath(source.getValue()), false));
        }
        return resolvedSources;
    }
}
