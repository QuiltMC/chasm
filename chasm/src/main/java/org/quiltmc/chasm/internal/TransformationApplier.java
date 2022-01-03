package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.target.NodeTarget;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.OriginMetadata;
import org.quiltmc.chasm.internal.metadata.ListPathMetadata;
import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.tree.LazyClassNode;

public class TransformationApplier {
    private final ListNode classes;
    private final List<Transformation> transformations;

    private final Map<ListPathMetadata, List<Target>> affectedTargets;

    public TransformationApplier(ListNode classes, List<Transformation> transformations) {
        this.classes = classes;
        this.transformations = transformations;

        this.affectedTargets = new HashMap<>();
    }

    private static ListPathMetadata getPath(Target target) {
        ListPathMetadata path = target.getTarget().getMetadata().get(ListPathMetadata.class);
        if (path == null) {
            throw new RuntimeException("Node in specified target is missing list information.");
        }
        return path;
    }

    private List<Target> getAffectedTargets(ListPathMetadata path) {
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
        Node target = resolveTarget(transformation.getTarget());
        MapNode sources = resolveSources(transformation);

        /*
         * I shouldn't pass the mutable target directly because then the transformer could mutate it.
         *
         * I shouldn't pass the target as an owned cow wrapper because that requires a deep copy anyway, or else the wrapper will assume the target's child nodes are owned as well.
         *
         * I shouldn't pass the target as a shared cow wrapper because that wouldn't allow me to unwrap it.
         * Maybe that's OK for now though.
         */
        Node replacement = transformation.apply(target.asWrapper(null, null, false),
                sources.asWrapper(null, null, false));
        replacement.getMetadata().put(OriginMetadata.class, new OriginMetadata(transformation));

        replaceTarget(transformation.getTarget(), replacement);
    }

    private void replaceTarget(Target target, Node replacement) {
        if (target instanceof NodeTarget) {
            replaceNode((NodeTarget) target, replacement);
        } else if (target instanceof SliceTarget && replacement instanceof ListNode) {
            replaceSlice((SliceTarget) target, Node.asList(replacement));
        } else {
            throw new IllegalArgumentException("Invalid replacement for target");
        }
    }

    private void replaceNode(NodeTarget nodeTarget, Node replacement) {
        ListPathMetadata targetPath = getPath(nodeTarget);

        int classIndex = targetPath.get(0).asInteger();
        if (classes.get(classIndex) instanceof LazyClassNode) {
            classes.set(classIndex, ((LazyClassNode) classes.get(classIndex)).getFullNode());
        }

        Node parentNode = targetPath.parent().resolve(classes);
        ListPathMetadata.Entry entry = targetPath.get(targetPath.size() - 1);

        if (parentNode instanceof ListNode && entry.isInteger()) {
            ListNode parentList = Node.asList(parentNode);
            parentList.set(entry.asInteger(), replacement);
            return;
        }

        if (parentNode instanceof MapNode && entry.isString()) {
            MapNode parentList = Node.asMap(parentNode);
            parentList.put(entry.asString(), replacement);
            return;
        }

        throw new IndexOutOfBoundsException("Invalid index into node.");
    }

    private void replaceSlice(SliceTarget sliceTarget, ListNode replacement) {
        ListPathMetadata targetPath = getPath(sliceTarget);

        int classIndex = targetPath.get(0).asInteger();
        if (classes.get(classIndex) instanceof LazyClassNode) {
            classes.set(classIndex, ((LazyClassNode) classes.get(classIndex)).getFullNode());
        }

        Node parentNode = targetPath.resolve(classes);

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

    private void movePathIndex(ListPathMetadata path, int pathIndex, int endIndex, int amount) {
        int originalIndex = path.get(pathIndex).asInteger();
        if (originalIndex >= endIndex) {
            path.set(pathIndex, new ListPathMetadata.Entry(originalIndex + amount));
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

    private Node resolveTarget(Target target) {
        Node currentNode = classes;
        PathMetadata path = getPath(target);

        for (ListPathMetadata.Entry entry : path) {
            if (currentNode instanceof ListNode && entry.isInteger()) {
                currentNode = Node.asList(currentNode).get(entry.asInteger());
            } else if (currentNode instanceof MapNode && entry.isString()) {
                currentNode = Node.asMap(currentNode).get(entry.asString());
            } else {
                throw new NoSuchElementException("Can't resolve list " + path);
            }
        }

        return currentNode;
    }

    private MapNode resolveSources(Transformation transformation) {
        MapNode resolvedSources = new LinkedHashMapNode();
        for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
            resolvedSources.put(source.getKey(), resolveTarget(source.getValue()));
        }
        return resolvedSources;
    }
}
