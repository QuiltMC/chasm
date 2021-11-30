package org.quiltmc.chasm.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quiltmc.chasm.api.Transformer;

public class TransformerSorter {
    public static List<List<Transformer>> sort(Collection<Transformer> transformers) {
        Map<String, TransformerInfo> infoById = new LinkedHashMap<>();

        // Create vertices
        for (Transformer transformer : transformers) {
            if (infoById.containsKey(transformer.getId())) {
                throw new RuntimeException("Duplicate transformer id: " + transformer.getId());
            }
            TransformerInfo vertex = new TransformerInfo(transformer);
            infoById.put(transformer.getId(), vertex);
        }

        // Create dependencies
        for (TransformerInfo info : infoById.values()) {
            Transformer transformer = info.get();

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

            // Extract transformers and remove remaining round dependencies
            List<Transformer> round = new ArrayList<>();
            for (TransformerInfo info : roundInfo) {
                info.removeRoundDependencies();
                round.add(info.get());
            }
            rounds.add(round);
        }

        return rounds;
    }

    static class TransformerInfo {
        private final Transformer transformer;

        private final Set<TransformerInfo> roundDependencies = new LinkedHashSet<>();
        private final Set<TransformerInfo> roundDependents = new LinkedHashSet<>();

        private final Set<TransformerInfo> dependencies = new LinkedHashSet<>();
        private final Set<TransformerInfo> dependents = new LinkedHashSet<>();

        public TransformerInfo(Transformer transformer) {
            this.transformer = transformer;
        }

        public Transformer get() {
            return transformer;
        }

        public void addDependency(TransformerInfo other) {
            this.dependencies.add(other);
            other.dependents.add(this);
        }

        public void addRoundDependency(TransformerInfo other) {
            roundDependencies.add(other);
            other.roundDependents.add(this);
        }

        public Set<TransformerInfo> getDependencies() {
            return dependencies;
        }

        public Set<TransformerInfo> getRoundDependencies() {
            return roundDependencies;
        }

        public void removeDependencies() {
            for (TransformerInfo info : dependents) {
                info.dependencies.remove(this);
            }
        }

        public void removeRoundDependencies() {
            for (TransformerInfo info : roundDependents) {
                info.roundDependencies.remove(this);
            }
        }
    }
}
