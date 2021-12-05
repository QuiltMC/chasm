package org.quiltmc.chasm.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.quiltmc.chasm.api.tree.ListNode;

/**
 * Bulk-instantiates {@link Transformation}s of classes.
 *
 * <p>{@link Transformer}s also provide a {@code String} ID for use in occasionally
 * hard-coding relative {@code Transformer} ordering.
 */
public interface Transformer {
    /**
     * Applies this {@link Transformer} to the given {@link ListNode} of classes,
     * resulting in a {@code Collection} of {@link Transformation}s.
     *
     * @param classes The {@code ListNode} of classes available to transform.
     *
     * @return A {@code Collection} of {@code Transformation}s this
     *             {@code Transformer} created from the given {@code ListNode} of classes.
     */
    Collection<Transformation> apply(ListNode classes);

    /**
     * Gets the ID of this {@link Transformer}.
     *
     * <p>The ID string <b>must</b> be a string unique among all other transformers.
     * E.g. org.example.transformers.ExampleTransformer
     *
     * @return The unique ID of this Transformer.
     */
    String getId();

    /**
     * Defines explicit dependencies between the {@link Transformation}s of {@link Transformer}s.
     * All Transformations defined by this Transformer *must run after* all Transformations
     * defined by all the Transformers whose ID is in the returned {@link Set}.
     *
     * @param transformerIds All known transformer IDs.
     * @return A set of Transformer IDs this Transformer must run after.
     *
     * @see #getId()
     */
    default Set<String> mustRunAfter(Set<String> transformerIds) {
        return Collections.emptySet();
    }

    /**
     * Defines explicit dependencies between the {@link Transformation}s of {@link Transformer}s.
     * All Transformations defined by this Transformer *must run before* all Transformations
     * defined by all the Transformers whose ID is in the returned {@link Set}.
     *
     * @param transformerIds All known transformer IDs.
     * @return A Set of Transformer IDs this Transformer must run before.
     *
     * @see #getId()
     */
    default Set<String> mustRunBefore(Set<String> transformerIds) {
        return Collections.emptySet();
    }


    /**
     * Defines explicit dependencies between {@link Transformer}s.
     * This Transformer *must run after* all the Transformers whose ID is in the returned {@link Set}.
     *
     * @param transformerIds All known transformer IDs.
     * @return A {@link Set} of Transformer IDs this Transformer must run after.
     *
     * @see #getId()
     */
    default Set<String> mustRunRoundAfter(Set<String> transformerIds) {
        return Collections.emptySet();
    }

    /**
     * Defines explicit dependencies between {@link Transformer}s.
     * This Transformer *must run before* all the Transformers whose ID is in the returned {@link Set}.
     *
     * @param transformerIds All known transformer IDs.
     * @return A {@link Set} of Transformer IDs this Transformer must run before.
     *
     * @see #getId()
     */
    default Set<String> mustRunRoundBefore(Set<String> transformerIds) {
        return Collections.emptySet();
    }
}
