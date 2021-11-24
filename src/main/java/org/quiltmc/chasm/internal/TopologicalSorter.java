package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;

public class TopologicalSorter {
    public static List<List<Transformer>> sortTransformers(List<Transformer> transformers) {
        return sort(transformers, (first, second) -> {
            // Only round dependencies must place transformers in different rounds
            if (first.mustRunRoundAfter(second.getId()) || second.mustRunRoundBefore(first.getId())) {
                return Dependency.STRONG;
            }

            return Dependency.NONE;
        });
    }

    public static List<Transformation> sortTransformations(List<Transformation> transformations) {
        return sort(transformations, (first, second) -> {
            // Strong dependencies inherited from defining Transformer
            if (first.getParent().mustRunAfter(second.getParent().getId())) {
                return Dependency.STRONG;
            }

            if (second.getParent().mustRunBefore(first.getParent().getId())) {
                return Dependency.STRONG;
            }

            // Case 6a/7a/8a
            // Any contained targets must be applied first.
            if (first.getTarget().contains(second.getTarget())) {
                return Dependency.STRONG;
            }

            // Case 6b/7b/8b
            // Any contained sources must be resolved first.
            if (second.getSources().values().stream().anyMatch(first.getTarget()::contains)) {
                return Dependency.STRONG;
            }

            //Case 2a
            // Any overlapping target must be applied .
            // Note that this is symmetric, so overlapping targets always form a dependency loop.
            if (first.getTarget().overlaps(second.getTarget())) {
                return Dependency.STRONG;
            }

            // Case 2b/2c
            // Any overlapping sources must be resolved first.
            if (second.getSources().values().stream().anyMatch(first.getTarget()::overlaps)) {
                return Dependency.STRONG;
            }

            //Case 6c/7c/8c
            // Any sources containing a target should be applied after the target if possible.
            // TODO: Re-evaluate if this should be a strong dependency instead
            if (first.getSources().values().stream().anyMatch(s -> s.contains(second.getTarget()))) {
                return Dependency.WEAK;
            }

            return Dependency.NONE;
        }).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public static <T> List<List<T>> sort(List<T> list, DependencyProvider<T> dependencyProvider) {
        // Create vertices
        // Note: LinkedHashSet is used to preserve insertion order
        LinkedHashSet<Vertex<T>> toSort = list.stream().map(Vertex::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Determine dependencies
        for (Vertex<T> first : toSort) {
            for (Vertex<T> second : toSort) {
                if (first == second) {
                    continue;
                }

                Dependency dependency = dependencyProvider.get(first.getValue(), second.getValue());
                if (dependency == Dependency.STRONG) {
                    first.addDependency(second);
                } else if (dependency == Dependency.WEAK) {
                    first.addWeakDependency(second);
                }
            }
        }

        // Note: LinkedHashSet is used to preserve insertion order
        List<List<T>> sorted = new ArrayList<>(toSort.size());

        while (!toSort.isEmpty()) {
            // Get all vertices without dependencies
            List<Vertex<T>> nextVertices =
                    toSort.stream().filter(v -> v.dependencies.isEmpty()).collect(Collectors.toList());

            if (!nextVertices.isEmpty()) {

                for (Vertex<T> vertex : nextVertices) {
                    // Remove dependencies
                    for (Vertex<T> dependent : vertex.dependencies) {
                        dependent.dependencies.remove(vertex);
                        dependent.weakDependencies.remove(vertex);
                    }

                    // Remove from remaining vertices
                    toSort.remove(vertex);
                }

                // Add to sorted
                sorted.add(nextVertices.stream().map(Vertex::getValue).collect(Collectors.toList()));
            } else {
                // TODO: This is definitely not optimal.
                //  Instead, try to find a Vertex with no hard dependencies and the least soft dependencies.
                //  Even better: Find all loops and break them all at the same time.
                // Can't sort, try breaking a weak dependency
                Optional<Vertex<T>> optNext = toSort.stream().filter(v -> !v.weakDependencies.isEmpty()).findFirst();

                if (optNext.isPresent()) {
                    Vertex<T> next = optNext.get();

                    // Get first weak dependency
                    Vertex<T> toBreak = next.weakDependencies.iterator().next();

                    // Emit warning
                    System.err.println("WARNING: Breaking weak dependency: "
                            + next.getValue().toString() + " dependes on " + toBreak.getValue().toString());

                    // Break dependency
                    next.dependencies.remove(toBreak);
                    next.weakDependencies.remove(toBreak);
                    toBreak.dependents.remove(next);
                } else {
                    // No more weak dependencies left to break, abort
                    throw new RuntimeException("Can't sort transformations.");
                }
            }
        }

        return sorted;
    }

    public enum Dependency {
        NONE,
        WEAK,
        STRONG
    }

    public interface DependencyProvider<T> {
        /**
         * Provides dependency information for two values.
         * This is unidirectional and only determines how the first value depends on the second.
         * If the reverse information is needed, the method has to be called with the parameters swapped.
         *
         * @param first The dependent value
         * @param second Teh dependency value
         * @return How first depends on second
         */
        Dependency get(T first, T second);
    }

    private static class Vertex<T> {
        /**
         * The value wrapped in this {@link Vertex}.
         */
        private final T value;

        /**
         * All the vertices this {@link Vertex} directly depends on.
         */
        private final Set<Vertex<T>> dependencies;

        /**
         * All the vertices this {@link Vertex} only weakly directly depends on.
         * Note that these vertices also exist in {@link #dependencies}.
         */
        private final Set<Vertex<T>> weakDependencies;

        /**
         * All the vertices that directly depend on this {@link Vertex}.
         */
        private final Set<Vertex<T>> dependents;

        public Vertex(T value) {
            this.value = value;
            // Note: LinkedHashSets are used to preserve insertion order
            this.dependencies = new LinkedHashSet<>();
            this.weakDependencies = new LinkedHashSet<>();
            this.dependents = new LinkedHashSet<>();
        }

        public T getValue() {
            return value;
        }

        public void addDependency(Vertex<T> dependency) {
            this.dependencies.add(dependency);
            dependency.dependents.add(this);
        }

        public void addWeakDependency(Vertex<T> dependency) {
            addDependency(dependency);
            this.weakDependencies.add(dependency);
        }
    }
}
