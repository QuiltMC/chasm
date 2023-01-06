package org.quiltmc.chasm;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.quiltmc.chasm.api.Transformation;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.internal.TransformerSorter;
import org.quiltmc.chasm.lang.api.ast.ListNode;

/**
 * Unit tests for {@link org.quiltmc.chasm.internal.TransformerSorter}.
 */
public class TransformerSorterTests {

    /**
     * If sorting goes on more than this long, {@link #assertThrowsWithinTimeout(Class, Executable)} will assume it is
     * stuck in a loop.
     */
    private static final Duration INFINITE_LOOP_DURATION = Duration.ofSeconds(1);

    /**
     * The sorter should reject an input where two transformers have the same ID.
     */
    @Test
    public void testRejectsDuplicateId() {
        Assertions.assertThrows(
                RuntimeException.class,
                () -> {
                    TransformerSorter.sort(Set.of(
                            new DummyTransformer("a", Set.of(), Set.of(), Set.of(), Set.of()),
                            new DummyTransformer("a", Set.of(), Set.of(), Set.of(), Set.of())
                    ));
                });
    }

    /**
     * The sorter should reject an input where there is a dependency loop.
     * Here, A must run after B, but B must run after A.
     */
    @Test
    public void testRejectsSimpleLoop() {
        assertThrowsWithinTimeout(
                RuntimeException.class,
                () -> {
                    TransformerSorter.sort(Set.of(
                            new DummyTransformer("a", Set.of("b"), Set.of(), Set.of(), Set.of()),
                            new DummyTransformer("b", Set.of("a"), Set.of(), Set.of(), Set.of())
                    ));
                });
    }

    /**
     * The sorter should reject an input where a transformer depends on itself.
     * This is another case of a dependency loop.
     */
    @Test
    public void testRejectsSelfDependency() {
        assertThrowsWithinTimeout(
                RuntimeException.class,
                () -> {
                    TransformerSorter.sort(Set.of(
                            new DummyTransformer("a", Set.of("a"), Set.of(), Set.of(), Set.of())
                    ));
                });
    }

    /**
     * The sorter should be able to sort A after B, since A must run after B.
     */
    @Test
    public void testSimpleAfterDependencyWorks() {
        var a = new DummyTransformer("a", Set.of("b"), Set.of(), Set.of(), Set.of());
        var b = new DummyTransformer("b", Set.of(), Set.of(), Set.of(), Set.of());
        var sorted = TransformerSorter.sort(Set.of(a, b));
        assertIsInOrder(List.of(b, a), sorted);
    }

    /**
     * The sorter should be able to sort A before B, since A must run before B.
     */
    @Test
    public void testSimpleBeforeDependencyWorks() {
        var a = new DummyTransformer("a", Set.of(), Set.of("b"), Set.of(), Set.of());
        var b = new DummyTransformer("b", Set.of(), Set.of(), Set.of(), Set.of());
        var sorted = TransformerSorter.sort(Set.of(a, b));
        assertIsInOrder(List.of(a, b), sorted);
    }

    /**
     * The sorter should be able to sort B in the first round, and A in the second round, since A mustRunRoundAfter B.
     */
    @Test
    public void testSimpleRoundAfterDependencyWorks() {
        var a = new DummyTransformer("a", Set.of(), Set.of(), Set.of("b"), Set.of());
        var b = new DummyTransformer("b", Set.of(), Set.of(), Set.of(), Set.of());
        var sorted = TransformerSorter.sort(Set.of(a, b));
        assertIsInOrderWithRounds(
                List.of(
                        List.of(b),
                        List.of(a)
                ),
                sorted
        );
    }

    /**
     * The sorter should be able to sort A in the first round, and B in the second round, since A mustRunRoundBefore B.
     */
    @Test
    public void testSimpleRoundBeforeDependencyWorks() {
        var a = new DummyTransformer("a", Set.of(), Set.of(), Set.of(), Set.of("b"));
        var b = new DummyTransformer("b", Set.of(), Set.of(), Set.of(), Set.of());
        var sorted = TransformerSorter.sort(Set.of(a, b));
        assertIsInOrderWithRounds(
                List.of(
                        List.of(a),
                        List.of(b)
                ),
                sorted
        );
    }

    /**
     * The sorter should be able to sort A before B before C, since B mustRunAfter A, and C mustRunAfter B.
     */
    @Test
    public void testTransitive() {
        var a = new DummyTransformer("a", Set.of(), Set.of(), Set.of(), Set.of());
        var b = new DummyTransformer("b", Set.of("a"), Set.of(), Set.of(), Set.of());
        var c = new DummyTransformer("c", Set.of("b"), Set.of(), Set.of(), Set.of());
        var sorted = TransformerSorter.sort(Set.of(a, b, c));
        assertIsInOrder(List.of(a, b, c), sorted);
    }

    /**
     * The sorter should be able to sort A before B before C, since A mustRunBefore B, and C mustRunAfter B, even though
     * B is not defined.
     */
    @Test
    public void testTransitiveWithUndefined() {
        var a = new DummyTransformer("a", Set.of(), Set.of("b"), Set.of(), Set.of());
        var c = new DummyTransformer("c", Set.of("b"), Set.of(), Set.of(), Set.of());
        var sorted = TransformerSorter.sort(Set.of(a, c));
        assertIsInOrder(List.of(a, c), sorted);
    }

    /**
     * The sorter should be able to sort A after C, since A mustRunAfter B and C, but B is not defined.
     */
    @Test
    public void testMissingDependencies() {
        var a = new DummyTransformer("a", Set.of("b", "c"), Set.of(), Set.of(), Set.of());
        var c = new DummyTransformer("c", Set.of(), Set.of(), Set.of(), Set.of());
        var sorted = TransformerSorter.sort(Set.of(a, c));
        assertIsInOrder(List.of(c, a), sorted);
    }

    /**
     * The sorter should reject this cycle involving a transitive dependency on a nonexistant transformer.
     */
    @Test
    public void testRejectsMissingDependenciesCycle() {
        var a = new DummyTransformer("a", Set.of("c"), Set.of("b"), Set.of(), Set.of());
        var c = new DummyTransformer("c", Set.of("b"), Set.of(), Set.of(), Set.of());
        assertThrowsWithinTimeout(
                RuntimeException.class,
                () -> TransformerSorter.sort(Set.of(a, c))
        );
    }

    /**
     * Asserts that the method throws the expected type within the timeout specified in {@link #INFINITE_LOOP_DURATION},
     * otherwise fails.
     *
     * @param expectedType  the class of the expected Throwable
     * @param executable    the executable to test
     * @param <T>           the type of the expected Throwable
     */
    private static <T extends Throwable> void assertThrowsWithinTimeout(Class<T> expectedType, Executable executable) {
        Assertions.assertTimeoutPreemptively(
                INFINITE_LOOP_DURATION,
                () -> Assertions.assertThrows(expectedType, executable)
        );
    }

    private static String transformersToString(List<Transformer> transformers) {
        return transformers.stream().map(Transformer::getId)
                .collect(
                        StringBuilder::new,
                        (b, s) -> b.append(b.isEmpty() ? "" : ", ").append(s),
                        StringBuilder::append
                )
                .toString();
    }

    private static void assertTransformerListsEqual(List<Transformer> expectedOrder, List<Transformer> actualOrder) {
        for (int i = 0; i < expectedOrder.size(); i++) {
            var expected = expectedOrder.get(i);
            var actual = actualOrder.get(i);
            if (expected != actual) {
                Assertions.fail(
                        "Transformer order was wrong. Expected [%s], got [%s].".formatted(
                                transformersToString(expectedOrder), transformersToString(actualOrder)
                        )
                );
            }
        }
    }


    private static void assertIsInOrder(List<Transformer> expectedOrder, List<List<Transformer>> sorted) {
        var flattened = sorted.stream().flatMap(List::stream).toList();
        if (flattened.size() != expectedOrder.size()) {
            Assertions.fail("Transformer count was wrong. Expected %s, got %s".formatted(
                            expectedOrder.size(), flattened.size()
                    ));
        }

        assertTransformerListsEqual(expectedOrder, flattened);
    }

    private static void assertIsInOrderWithRounds(
            List<List<Transformer>> expectedOrderWithRounds,
            List<List<Transformer>> sorted
    ) {
        if (expectedOrderWithRounds.size() != sorted.size()) {
            Assertions.fail("Round count was wrong. Expected %s, got %s".formatted(
                    expectedOrderWithRounds.size(), sorted.size()
            ));
        }

        for (int i = 0; i < expectedOrderWithRounds.size(); i++) {
            var expected = expectedOrderWithRounds.get(i);
            var actual = sorted.get(i);
            assertTransformerListsEqual(expected, actual);
        }
    }

    private static class DummyTransformer implements Transformer {

        private final String id;
        private final Set<String> mustRunAfter;
        private final Set<String> mustRunBefore;
        private final Set<String> mustRunRoundAfter;
        private final Set<String> mustRunRoundBefore;

        private DummyTransformer(String id, Set<String> mustRunAfter, Set<String> mustRunBefore,
                                 Set<String> mustRunRoundAfter, Set<String> mustRunRoundBefore) {
            this.id = id;
            this.mustRunAfter = mustRunAfter;
            this.mustRunBefore = mustRunBefore;
            this.mustRunRoundAfter = mustRunRoundAfter;
            this.mustRunRoundBefore = mustRunRoundBefore;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public Set<String> mustRunAfter(Set<String> transformerIds) {
            return this.mustRunAfter;
        }

        @Override
        public Set<String> mustRunBefore(Set<String> transformerIds) {
            return this.mustRunBefore;
        }

        @Override
        public Set<String> mustRunRoundAfter(Set<String> transformerIds) {
            return this.mustRunRoundAfter;
        }

        @Override
        public Set<String> mustRunRoundBefore(Set<String> transformerIds) {
            return this.mustRunRoundBefore;
        }

        @Override
        public Collection<Transformation> apply(ListNode classes) {
            return Collections.emptyList();
        }
    }
}
