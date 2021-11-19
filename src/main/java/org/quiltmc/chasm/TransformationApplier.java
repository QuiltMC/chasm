package org.quiltmc.chasm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.transformer.NodePath;
import org.quiltmc.chasm.transformer.NodeTarget;
import org.quiltmc.chasm.transformer.SliceTarget;
import org.quiltmc.chasm.transformer.Target;
import org.quiltmc.chasm.transformer.Transformation;
import org.quiltmc.chasm.tree.LinkedHashMapNode;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;

public class TransformationApplier {
    private final ListNode classes;
    private final List<Transformation> transformations;

    private final Map<NodePath, List<Target>> affectedTargets;

    public TransformationApplier(ListNode classes, List<Transformation> transformations) {
        this.classes = classes;
        this.transformations = transformations;

        this.affectedTargets = new HashMap<>();
    }

    private List<Target> getAffectedTargets(NodePath path) {
        List<Target> affectedTargets = new ArrayList<>();

        for (Transformation transformation : transformations) {
            List<Target> targets = new ArrayList<>(transformation.getSources().values());
            targets.add(transformation.getTarget());

            for (Target target : targets) {
                if (target instanceof SliceTarget && path.contains(((SliceTarget) target).getPath())) {
                    affectedTargets.add(target);
                }

                if (target instanceof NodeTarget && path.contains(((NodeTarget) target).getPath().getParent())) {
                    affectedTargets.add(target);
                }
            }
        }

        return affectedTargets;
    }

    public void applyAll() {
        Map<NodePath, List<Target>> affectedTargets = new HashMap<>();
        for (Transformation transformation : transformations) {
            applyTransformation(transformation);
        }
    }

    private void applyTransformation(Transformation transformation) {
        Node target = resolveTarget(transformation);
        MapNode sources = resolveSources(transformation);

        Node replacement = transformation.apply(target, sources);
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
        int classIndex = (Integer) nodeTarget.getPath().getEntryAt(0);
        if (classes.get(classIndex) instanceof LazyClassNode) {
            classes.set(classIndex, ((LazyClassNode) classes.get(classIndex)).getFullNode());
        }

        Node parentNode = nodeTarget.getPath().getParent().resolve(classes);
        Object index = nodeTarget.getPath().getLastEntry();

        if (parentNode instanceof ListNode && index instanceof Integer) {
            ListNode parentList = (ListNode) parentNode;
            parentList.set((int) index, replacement);
            return;
        }

        if (parentNode instanceof MapNode && index instanceof String) {
            MapNode parentList = (MapNode) parentNode;
            parentList.put((String) index, replacement);
            return;
        }

        throw new RuntimeException("Invalid index into node.");
    }

    private void replaceSlice(SliceTarget sliceTarget, ListNode replacement) {
        int classIndex = (Integer) sliceTarget.getPath().getEntryAt(0);
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
                this.affectedTargets.computeIfAbsent(sliceTarget.getPath(), this::getAffectedTargets);
        for (Target target : affectedTargets) {
            if (target instanceof NodeTarget) {
                movePathIndex(((NodeTarget) target).getPath(), sliceTarget.getPath().getLength(), end, change);
            }

            if (target instanceof SliceTarget) {
                if (((SliceTarget) target).getPath().equals(sliceTarget.getPath())) {
                    moveSliceIndex((SliceTarget) target, end, change);
                } else {
                    movePathIndex(((SliceTarget) target).getPath(), sliceTarget.getPath().getLength(), end, change);
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

    private void movePathIndex(NodePath path, int pathIndex, int endIndex, int amount) {
        int originalIndex = (Integer) path.getEntryAt(pathIndex);
        if (originalIndex >= endIndex) {
            path.setEntryAt(pathIndex, originalIndex + amount);
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
        return transformation.getTarget().resolve(classes).toImmutable();
    }

    private MapNode resolveSources(Transformation transformation) {
        MapNode resolvedSources = new LinkedHashMapNode();
        for (Map.Entry<String, Target> source : transformation.getSources().entrySet()) {
            resolvedSources.put(source.getKey(), source.getValue().resolve(classes).toImmutable());
        }
        return resolvedSources;
    }
}
