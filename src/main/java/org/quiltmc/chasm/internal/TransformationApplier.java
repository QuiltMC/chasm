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

    private List<Target> getAffectedTargets(PathMetadata path) {
        List<Target> affectedTargets = new ArrayList<>();

        for (Transformation transformation : transformations) {
            List<Target> targets = new ArrayList<>(transformation.getSources().values());
            targets.add(transformation.getTarget());

            for (Target target : targets) {
                if (target instanceof SliceTarget && ((SliceTarget) target).getPath().startsWith(path)) {
                    affectedTargets.add(target);
                }

                if (target instanceof NodeTarget && ((NodeTarget) target).getPath().parent().startsWith(path)) {
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
        Node target = resolveTarget(transformation);
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
            replaceSlice((SliceTarget) target, (ListNode) replacement);
        } else {
            throw new RuntimeException("Invalid replacement for target");
        }

    }

    private void replaceNode(NodeTarget nodeTarget, Node replacement) {
        PathMetadata targetPath = nodeTarget.getPath();

        int classIndex = targetPath.get(0).asInteger();
        if (classes.get(classIndex) instanceof LazyClassNode) {
            classes.set(classIndex, ((LazyClassNode) classes.get(classIndex)).getFullNode());
        }

        Node parentNode = targetPath.parent().resolve(classes);
        PathMetadata.Entry entry = targetPath.get(targetPath.size() - 1);

        if (parentNode instanceof ListNode && entry.isInteger()) {
            ListNode parentList = (ListNode) parentNode;
            parentList.set(entry.asInteger(), replacement);
            return;
        }

        if (parentNode instanceof MapNode && entry.isString()) {
            MapNode parentList = (MapNode) parentNode;
            parentList.put(entry.asString(), replacement);
            return;
        }

        throw new RuntimeException("Invalid index into node.");
    }

    private void replaceSlice(SliceTarget sliceTarget, ListNode replacement) {
        PathMetadata targetPath = sliceTarget.getPath();

        int classIndex = targetPath.get(0).asInteger();
        if (classes.get(classIndex) instanceof LazyClassNode) {
            classes.set(classIndex, ((LazyClassNode) classes.get(classIndex)).getFullNode());
        }

        Node parentNode = sliceTarget.getPath().resolve(classes);

        if (!(parentNode instanceof ListNode)) {
            throw new UnsupportedOperationException("Replacement for slice target must be a list node.");
        }

        ListNode parentList = (ListNode) parentNode;

        int change = parentList.size() - replacement.size();
        int start = sliceTarget.getStartIndex() / 2;
        int end = sliceTarget.getEndIndex() / 2;
        int length = end - start;

        // Move all slice indices affected by this
        List<Target> affectedTargets =
                this.affectedTargets.computeIfAbsent(targetPath, this::getAffectedTargets);
        for (Target target : affectedTargets) {
            if (target instanceof NodeTarget) {
                movePathIndex(((NodeTarget) target).getPath(), targetPath.size(), end, change);
            }

            if (target instanceof SliceTarget) {
                if (((SliceTarget) target).getPath().equals(sliceTarget.getPath())) {
                    moveSliceIndex((SliceTarget) target, end, change);
                } else {
                    movePathIndex(((SliceTarget) target).getPath(), targetPath.size(), end, change);
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

    private Node resolveTarget(Transformation transformation) {
        return transformation.getTarget().resolve(classes);
    }

    private MapNode resolveSources(Transformation transformation) {
        MapNode resolvedSources = new LinkedHashMapNode();
        for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
            resolvedSources.put(source.getKey(), source.getValue().resolve(classes));
        }
        return resolvedSources;
    }
}
