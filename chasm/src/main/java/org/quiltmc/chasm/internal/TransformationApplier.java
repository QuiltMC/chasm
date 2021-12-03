package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.target.NodeTarget;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathEntry;
import org.quiltmc.chasm.internal.metadata.OriginMetadata;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public class TransformationApplier {
    private final ListNode<Node> classes;
    private final List<Transformation> transformations;

    private final Map<PathMetadata, List<Target>> affectedTargets;

    public TransformationApplier(ListNode classes, List<Transformation> transformations) {
        this.classes = classes;
        this.transformations = transformations;

        affectedTargets = new HashMap<>();
    }

    private static PathMetadata getPath(Target target) {
        PathMetadata path = (PathMetadata) target.getTarget().getMetadata().get(PathMetadata.class);
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
        Node target = resolveTarget(transformation.getTarget());
        MapNode<Node> sources = resolveSources(transformation);

        // TODO: Replace copies with immutability
        Node replacement = transformation.apply(target.asImmutable(), sources.asImmutable()).asImmutable();
        // What? replacement is immutable and this tries to mutate it.
        replacement.getMetadata().put(new OriginMetadata(transformation));

        replaceTarget(transformation.getTarget(), replacement);
    }

    @SuppressWarnings("unchecked")
    private void replaceTarget(Target target, Node replacement) {
        if (target instanceof NodeTarget) {
            replaceNode((NodeTarget) target, replacement);
        } else if (target instanceof SliceTarget && replacement instanceof ListNode) {
            replaceSlice((SliceTarget) target, (ListNode<Node>) replacement);
        } else {
            throw new RuntimeException("Invalid replacement for target");
        }

    }

    private void replaceNode(NodeTarget nodeTarget, Node replacement) {
        PathMetadata targetPath = getPath(nodeTarget);

        int classIndex = targetPath.get(0).asInteger();
        if (classes.get(classIndex) instanceof LazyClassNode) {
            classes.set(classIndex, ((LazyClassNode) classes.get(classIndex)).getFullNode());
        }

        Node parentNode = targetPath.parent().resolve(classes);
        PathEntry pathEntry = targetPath.get(targetPath.size() - 1);

        if (parentNode instanceof ListNode && pathEntry.isInteger()) {
            @SuppressWarnings("unchecked")
            ListNode<Node> parentList = (ListNode<Node>) parentNode;
            parentList.set(pathEntry.asInteger(), replacement);
            return;
        }

        if (parentNode instanceof MapNode && pathEntry.isString()) {
            @SuppressWarnings("unchecked")
            MapNode<Node> parentList = (MapNode<Node>) parentNode;
            parentList.put(pathEntry.toString(), replacement);
            return;
        }

        throw new RuntimeException("Invalid index into node.");
    }

    private void replaceSlice(SliceTarget sliceTarget, ListNode<Node> replacement) {
        PathMetadata targetPath = getPath(sliceTarget);

        int classIndex = targetPath.get(0).asInteger();
        if (classes.get(classIndex) instanceof LazyClassNode) {
            classes.set(classIndex, ((LazyClassNode) classes.get(classIndex)).getFullNode());
        }

        Node parentNode = targetPath.resolve(classes);

        if (!(parentNode instanceof ListNode)) {
            throw new UnsupportedOperationException("Replacement for slice target must be a list node.");
        }

        @SuppressWarnings("unchecked")
        ListNode<Node> parentList = (ListNode<Node>) parentNode;

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
            path.set(pathIndex, new PathEntry(originalIndex + amount));
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

    @SuppressWarnings("unchecked")
    private Node resolveTarget(Target target) {
        Node currentNode = classes;
        PathMetadata path = getPath(target);

        for (PathEntry pathEntry : path) {
            if (currentNode instanceof ListNode && pathEntry.isInteger()) {
                currentNode = ((ListNode<Node>) currentNode).get(pathEntry.asInteger());
            } else if (currentNode instanceof MapNode && pathEntry.isString()) {
                currentNode = ((MapNode<Node>) currentNode).get(pathEntry.toString());
            } else {
                throw new RuntimeException("Can't resolve path " + path);
            }
        }

        return currentNode;
    }

    private MapNode<Node> resolveSources(Transformation transformation) {
        MapNode<Node> resolvedSources = new LinkedHashMapNode();
        for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
            resolvedSources.put(source.getKey(), resolveTarget(source.getValue()));
        }
        return resolvedSources;
    }
}
