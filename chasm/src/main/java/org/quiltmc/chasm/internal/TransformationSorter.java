package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.target.SliceTarget;
import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public class TransformationSorter {
    public static List<Transformation> sort(Collection<Transformation> transformations) {
        List<TransformationInfo> infos = transformations.stream().map(TransformationInfo::new)
                .collect(Collectors.toCollection(LinkedList::new));

        computeDependencies(infos);

        List<Transformation> sorted = new ArrayList<>(transformations.size());

        while (!infos.isEmpty()) {
            boolean breakSoftDependencies = true;
            int minSoftDependencies = 0;
            TransformationInfo softToBreak = null;

            Iterator<TransformationInfo> iterator = infos.iterator();
            while (iterator.hasNext()) {
                TransformationInfo next = iterator.next();
                if (next.getHardDependencies().isEmpty()) {
                    if (next.getSoftDependencies().isEmpty()) {
                        // If no more dependencies, remove
                        next.remove();
                        iterator.remove();
                        sorted.add(next.get());
                        // No need to break soft dependencies
                        breakSoftDependencies = false;
                    } else {
                        // If only soft dependencies, keep track of node with the least of them
                        int softDependencies = next.getSoftDependencies().size();
                        if (softDependencies < minSoftDependencies || softToBreak == null) {
                            minSoftDependencies = softDependencies;
                            softToBreak = next;
                        }
                    }
                }

                // If no node was removed this iteration, break a soft dependency
                if (breakSoftDependencies) {
                    if (softToBreak == null) {
                        // If there is no node with only soft dependencies, we can't sort the given set
                        throw new RuntimeException("Can't sort the give Transformations");
                    }

                    // Remove node and add it to sorted
                    infos.remove(softToBreak);
                    softToBreak.remove();
                    sorted.add(softToBreak.get());
                }
            }
        }

        return sorted;
    }

    private static void computeDependencies(Collection<TransformationInfo> transformations) {
        // Group by Transformer ID
        Map<String, List<TransformationInfo>> byTransformerId = new HashMap<>();
        for (TransformationInfo info : transformations) {
            List<TransformationInfo> transformerVertices = byTransformerId
                    .computeIfAbsent(info.get().getParent().getId(), s -> new ArrayList<>());
            transformerVertices.add(info);
        }

        // Add explicit dependencies
        for (TransformationInfo transformation : transformations) {
            for (String id : transformation.get().getParent().mustRunAfter(byTransformerId.keySet())) {
                for (TransformationInfo other : byTransformerId.get(id)) {
                    transformation.addDependency(other);
                }
            }
            for (String id : transformation.get().getParent().mustRunBefore(byTransformerId.keySet())) {
                for (TransformationInfo other : byTransformerId.get(id)) {
                    other.addDependency(transformation);
                }
            }
        }

        // Extract target information
        List<TargetInfo> targets = new ArrayList<>();
        for (TransformationInfo transformation : transformations) {
            targets.add(new TargetInfo(transformation, transformation.get().getTarget(), TargetType.TARGET));

            for (Target target : transformation.get().getSources().values()) {
                targets.add(new TargetInfo(transformation, target, TargetType.SOURCE));
            }
        }

        // Recurse into target tree
        recurseTargets(targets, 0, new LinkedHashSet<>());
    }

    private static void recurseTargets(List<TargetInfo> targets, int depth, Set<TargetInfo> enclosingTargets) {
        // Group by path
        Map<PathMetadata.Entry, List<TargetInfo>> childrenByKey = new LinkedHashMap<>();
        for (TargetInfo target : targets) {
            PathMetadata path = target.getPath();
            PathMetadata.Entry entry = path.size() > depth ? path.get(depth) : null;
            childrenByKey.computeIfAbsent(entry, e -> new ArrayList<>());
        }

        // All Targets targeting the current node
        List<TargetInfo> currentTargets = childrenByKey.remove(null);
        currentTargets = currentTargets == null ? Collections.emptyList() : currentTargets;

        // NodeTargets target the entire node.
        List<TargetInfo> nodeTargets = currentTargets.stream()
                .filter(t -> t.getTarget() instanceof SliceTarget).collect(Collectors.toList());

        // All enclosing targets depend on all node targets
        for (TargetInfo enclosingTarget : enclosingTargets) {
            for (TargetInfo nodeTarget : nodeTargets) {
                enclosingTarget.addDependency(nodeTarget);
            }
        }

        // Start of all node targets
        enclosingTargets.addAll(nodeTargets);

        // Node targets only target part of the node
        List<TargetInfo> sliceTargets = currentTargets.stream()
                .filter(t -> t.getTarget() instanceof SliceTarget).collect(Collectors.toList());

        if (sliceTargets.isEmpty()) {
            // If there are no slice targets, simply descend into child nodes
            for (List<TargetInfo> children : childrenByKey.values()) {
                recurseTargets(children, depth + 1, enclosingTargets);
            }
        } else {
            // Collect slice start and end indices
            Map<Integer, List<TargetInfo>> targetsByStart = new LinkedHashMap<>();
            Map<Integer, List<TargetInfo>> targetsByEnd = new LinkedHashMap<>();
            Map<Integer, List<TargetInfo>> zeroLengthTargets = new LinkedHashMap<>();

            for (TargetInfo targetInfo : sliceTargets) {
                SliceTarget slice = (SliceTarget) targetInfo.getTarget();
                int start = slice.getStartIndex();
                int end = slice.getEndIndex();

                if (start == end) {
                    zeroLengthTargets.computeIfAbsent(start, i -> new ArrayList<>()).add(targetInfo);
                } else {
                    targetsByStart.computeIfAbsent(slice.getStartIndex(), i -> new ArrayList<>()).add(targetInfo);
                    targetsByEnd.computeIfAbsent(slice.getEndIndex(), i -> new ArrayList<>()).add(targetInfo);
                }
            }

            // Iterate over slice
            int index = 0;
            while (!childrenByKey.isEmpty() && !targetsByStart.isEmpty() && !targetsByEnd.isEmpty()) {
                // Get all targets ending at this index
                List<TargetInfo> endTargets = targetsByEnd.getOrDefault(index, Collections.emptyList());

                // Remove from enclosing targets
                enclosingTargets.removeAll(endTargets);

                // All remaining enclosing targets depend on all slice ends
                for (TargetInfo enclosingTarget : enclosingTargets) {
                    for (TargetInfo endTarget : endTargets) {
                        enclosingTarget.addDependency(endTarget);
                    }
                }

                // Get all zero-length targets at this index
                List<TargetInfo> zeroTargets = zeroLengthTargets.getOrDefault(index, Collections.emptyList());

                // All enclosing targets depend on all zero-length targets
                for (TargetInfo enclosingTarget : enclosingTargets) {
                    for (TargetInfo zeroTarget : zeroTargets) {
                        enclosingTarget.addDependency(zeroTarget);
                    }
                }

                // Get all targets starting at this index
                List<TargetInfo> startTargets = targetsByStart.getOrDefault(index, Collections.emptyList());

                // All enclosing targets depend on all slice starts
                for (TargetInfo enclosingTarget : enclosingTargets) {
                    for (TargetInfo startTarget : startTargets) {
                        enclosingTarget.addDependency(startTarget);
                    }
                }

                // Add to enclosing targets
                enclosingTargets.addAll(startTargets);

                if (index % 2 != 0) {
                    // Convert slice index to node index
                    PathMetadata.Entry entry = new PathMetadata.Entry(index / 2);
                    List<TargetInfo> children = childrenByKey.getOrDefault(entry, Collections.emptyList());
                    recurseTargets(children, depth + 1, enclosingTargets);
                }

                // Increment index
                index++;
            }
        }

        // End of all node targets
        enclosingTargets.removeAll(nodeTargets);
    }

    enum TargetType {
        TARGET,
        SOURCE
    }

    static class TargetInfo {
        private final TransformationInfo parent;
        private final Target target;
        private final TargetType type;

        private final PathMetadata path;

        public TargetInfo(TransformationInfo parent, Target target, TargetType type) {
            this.parent = parent;
            this.target = target;
            this.type = type;

            this.path = target.getTarget().getMetadata().get(PathMetadata.class);
        }

        public PathMetadata getPath() {
            return path;
        }

        public Target getTarget() {
            return target;
        }

        public TransformationInfo getParent() {
            return parent;
        }

        public void addDependency(TargetInfo other) {
            // A source always depends on a target softly
            if (this.type == TargetType.SOURCE && other.type == TargetType.TARGET) {
                parent.addSoftDependency(other.parent);
            }
            // A target always has hard dependencies
            if (this.type == TargetType.TARGET) {
                parent.addDependency(other.parent);
            }
        }
    }

    static class TransformationInfo {
        private final Transformation transformation;

        private final Set<TransformationInfo> dependencies = new LinkedHashSet<>();
        private final Set<TransformationInfo> softDependencies = new LinkedHashSet<>();

        private final Set<TransformationInfo> dependents = new LinkedHashSet<>();
        private final Set<TransformationInfo> softDependents = new LinkedHashSet<>();

        public TransformationInfo(Transformation transformation) {
            this.transformation = transformation;
        }

        public Transformation get() {
            return transformation;
        }

        public void addDependency(TransformationInfo other) {
            this.dependencies.add(other);
            other.dependents.add(this);
        }

        public void addSoftDependency(TransformationInfo other) {
            this.softDependencies.add(other);
            other.softDependents.add(this);
        }

        public Set<TransformationInfo> getHardDependencies() {
            return dependencies;
        }

        public Set<TransformationInfo> getSoftDependencies() {
            return softDependencies;
        }

        public void remove() {
            for (TransformationInfo info : dependents) {
                info.dependencies.remove(this);
            }
            for (TransformationInfo info : softDependents) {
                info.softDependencies.remove(this);
            }
        }
    }
}
