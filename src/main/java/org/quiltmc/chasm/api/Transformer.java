package org.quiltmc.chasm.api;

import java.util.Collection;

import org.quiltmc.chasm.api.tree.ListNode;

/**
 * A {@link Transformer} is a factory for bulk-instantiating {@link Transformation}s of classes.
 * 
 * They also have a {@link String} ID for use in hard-coding relative {@link Transformer} ordering.
 */
public interface Transformer {
    /**
     * @param classes The {@link ListNode} of classes to possibly instantiate this {@link Transformer} on.
     * 
     * @return The {@link Collection} of {@link Transformation}s this {@link Transformer}
     *         created from the given {@link ListNode} of classes.
     */
    Collection<Transformation> apply(ListNode classes);

    /**
     * @return This {@link Transformer}'s {@link Transformer} ID {@link String}.
     */
    String getId();

    /**
     * @param transformerId The {@link Transformer} ID {@link String} of a {@link Transformer}
     *        to check for required ordering.
     * 
     * @return Whether the {@link Transformer} with the given ID must be run after this {@link Transformer}.
     */
    default boolean mustRunAfter(String transformerId) {
        return false;
    }

    /**
     * @param transformerId The {@link Transformer} ID {@link String} of a {@link Transformer}
     *        to check for required ordering.
     * 
     * @return Whether the {@link Transformer} with the given ID must be run before this {@link Transformer}. 
     */
    default boolean mustRunBefore(String transformerId) {
        return false;
    }

    /**
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
