package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.api.Transformer;

public class TransformerSorter {
    public static List<List<Transformer>> sort(Collection<Transformer> transformers) {
        Map<String, TransformerInfo> infoById = new LinkedHashMap<>();

        // Create vertices for transformers that exist
        for (Transformer transformer : transformers) {
            if (infoById.containsKey(transformer.getId())) {
                throw new RuntimeException("Duplicate transformer id: " + transformer.getId());
            }
            TransformerInfo vertex = new TransformerInfo(transformer);
            infoById.put(transformer.getId(), vertex);
        }

        // Create vertices for transformers that don't exist.
        {
            Set<String> knownIds = new HashSet<>(infoById.keySet());
            transformers.stream()
                    .flatMap(transformer -> Stream.concat(
                            Stream.concat(
                                    transformer.mustRunAfter(knownIds).stream(),
                                    transformer.mustRunBefore(knownIds).stream()
                            ),
                            Stream.concat(
                                    transformer.mustRunRoundAfter(knownIds).stream(),
                                    transformer.mustRunBefore(knownIds).stream()
                            )
                    ))
                    .distinct()
                    .filter(s -> !infoById.containsKey(s))
                    .forEach(s -> infoById.put(s, new TransformerInfo(null)));
        }

        // Create dependencies
        for (TransformerInfo info : infoById.values()) {
            @Nullable Transformer transformer = info.get();
            if (transformer == null) {
                continue;
            }

            // mustRunAfter
            for (String otherId : transformer.mustRunAfter(infoById.keySet())) {
                TransformerInfo other = infoById.get(otherId);
                if (other == null) {
                    throw new RuntimeException("Unknown dependency " + otherId + " required by " + transformer.getId());
                }
                info.addDependency(other);
            }

            // mustRunBefore
            for (String otherId : transformer.mustRunBefore(infoById.keySet())) {
                TransformerInfo other = infoById.get(otherId);
                if (other == null) {
                    throw new RuntimeException("Unknown dependency " + otherId + " required by " + transformer.getId());
                }
                other.addDependency(info);
            }

            // mustRunRoundAfter
            for (String otherId : transformer.mustRunRoundAfter(infoById.keySet())) {
                TransformerInfo other = infoById.get(otherId);
                if (other == null) {
                    throw new RuntimeException("Unknown dependency " + otherId + " required by " + transformer.getId());
                }
                info.addRoundDependency(other);
            }

            // mustRunRoundBefore
            for (String otherId : transformer.mustRunRoundBefore(infoById.keySet())) {
                TransformerInfo other = infoById.get(otherId);
                if (other == null) {
                    throw new RuntimeException("Unknown dependency " + otherId + " required by " + transformer.getId());
                }
                other.addRoundDependency(info);
            }
        }

        // The remaining Transformers that need to be sorted
        List<TransformerInfo> remaining = new LinkedList<>(infoById.values());

        // The transformers grouped into round satisfying the dependencies
        List<List<Transformer>> rounds = new ArrayList<>();

        // Loop until all transformers are added
        while (!remaining.isEmpty()) {
            List<TransformerInfo> roundInfo = new ArrayList<>();

            boolean checkAgain;
            do {
                checkAgain = false;

                // Iterate remaining transformers
                Iterator<TransformerInfo> iterator = remaining.iterator();
                while (iterator.hasNext()) {
                    TransformerInfo info = iterator.next();

                    // Transformers with no remaining dependencies can be added to round
                    if (info.getDependencies().isEmpty() && info.getRoundDependencies().isEmpty()) {
                        // Remove from remaining and add to current round
                        iterator.remove();
                        roundInfo.add(info);

                        // Remove the dependencies any transformer has on this transformer
                        info.removeDependencies();

                        // Recheck the list because dependencies changed
                        checkAgain = true;
                    }
                }
            } while (checkAgain);

            // If no roundInfos were added, then dependencies did not change. Therefore, no roundInfos will be added
            // next loop either, so we're in an infinite loop.
            if (roundInfo.isEmpty()) {
                throw new RuntimeException("Dependency cycle in transformer sorting.");
            }

            // Extract transformers and remove remaining round dependencies
            List<Transformer> round = new ArrayList<>();
            for (TransformerInfo info : roundInfo) {
                info.removeRoundDependencies();
                @Nullable Transformer transformer = info.get();
                if (transformer != null) {
                    round.add(transformer);
                }
            }
            rounds.add(round);
        }

        return rounds;
    }

    static class TransformerInfo {
        private final @Nullable Transformer transformer;

        private final Set<TransformerInfo> roundDependencies = new LinkedHashSet<>();
        private final Set<TransformerInfo> roundDependents = new LinkedHashSet<>();

        private final Set<TransformerInfo> dependencies = new LinkedHashSet<>();
        private final Set<TransformerInfo> dependents = new LinkedHashSet<>();

        public TransformerInfo(@Nullable Transformer transformer) {
            this.transformer = transformer;
        }

        public @Nullable Transformer get() {
            return this.transformer;
        }

        public void addDependency(TransformerInfo other) {
            this.dependencies.add(other);
            other.dependents.add(this);
        }

        public void addRoundDependency(TransformerInfo other) {
            this.roundDependencies.add(other);
            other.roundDependents.add(this);
        }

        public Set<TransformerInfo> getDependencies() {
            return this.dependencies;
        }

        public Set<TransformerInfo> getRoundDependencies() {
            return this.roundDependencies;
        }

        public void removeDependencies() {
            for (TransformerInfo info : this.dependents) {
                info.dependencies.remove(this);
            }
        }

        public void removeRoundDependencies() {
            for (TransformerInfo info : this.roundDependents) {
                info.roundDependencies.remove(this);
            }
        }
    }
}
