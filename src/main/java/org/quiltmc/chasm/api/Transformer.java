package org.quiltmc.chasm.api;

import java.util.Collection;

import org.quiltmc.chasm.api.tree.ListNode;

/**
 * A {@link Transformer} is a factory for bulk-instantiating {@link Transformation}s of classes.
 *
 * <p>They also have a {@link String} ID for use in hard-coding relative {@link Transformer} ordering.
 */
public interface Transformer {
    /**
     * This method applies this {@link Transformer} to the given {@link ListNode} of classes,
     *          resulting in a {@link Collection} of {@link Transformation}s.
     *
     * @param classes The {@link ListNode} of classes to possibly instantiate this {@link Transformer} on.
     *
     * @return The {@link Collection} of {@link Transformation}s this {@link Transformer}
     *         created from the given {@link ListNode} of classes.
     */
    Collection<Transformation> apply(ListNode classes);

    /**
     * A getter method for this {@link Transformer}'s ID {@link String}.
     *
     * @return This {@link Transformer}'s {@link Transformer} ID {@link String}.
     */
    String getId();

    /**
     * A method to allow hard-coding {@link Transformer} ordering.
     *
     * <p>This method lets the programmer respond to {@link Transformer} ID {@link String}s
     *           that must run after this {@link Transformer}.
     *
     * @param transformerId The {@link Transformer} ID {@link String} of a {@link Transformer}
     *        to check for required ordering.
     *
     * @return Whether the {@link Transformer} with the given ID must be run after this {@link Transformer}.
     */
    default boolean mustRunAfter(String transformerId) {
        return false;
    }

    /**
     * A method to allow hard-coding {@link Transformer} ordering.
     *
     * <p>This method lets the programmer respond to {@link Transformer} ID {@link String}s
     *           that must run before this {@link Transformer}.
     *
     * @param transformerId The {@link Transformer} ID {@link String} of a {@link Transformer}
     *        to check for required ordering.
     *
     * @return Whether the {@link Transformer} with the given ID must be run before this {@link Transformer}. 
     */
    default boolean mustRunBefore(String transformerId) {
        return false;
    }

    /**
     * A method to allow hard-coding {@link Transformer} round ordering.
     *
     * <p>This method lets the programmer respond to {@link Transformer} ID {@link String}s
     *           whose round must run after this {@link Transformer}'s round.
     *
     * @param transformerId The {@link Transformer} ID {@link String} of a {@link Transformer}
     *        to check for required round ordering.
     *
     * @return Whether the round containing the {@link Transformer} with the given ID must be run after the
     *         round containing this {@link Transformer}.
     */
    default boolean mustRunRoundAfter(String transformerId) {
        return false;
    }

    /**
     * A method to allow hard-coding {@link Transformer} round ordering.
     *
     * <p>This method lets the programmer respond to {@link Transformer} ID {@link String}s
     *           whose round must run before this {@link Transformer}'s round.
     *
     * @param transformerId The {@link Transformer} ID {@link String} of a {@link Transformer}
     *        to check for required round ordering.
     *
     * @return Whether the round containing the {@link Transformer} with the given ID must be run before the
     *         round containing this {@link Transformer}.
     */
    default boolean mustRunRoundBefore(String transformerId) {
        return false;
    }
}
