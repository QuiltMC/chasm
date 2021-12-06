package org.quiltmc.chasm.api;

import java.util.Collections;
import java.util.Map;

import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.Node;

/**
 * Transforms a {@link Target} {@link Node} using requested {@code Node} sources.
 *
 * @see Function
 */
public interface Transformation {
    /**
     * Gets this {@link Transformation Transformation's} parent {@link Transformer}.
     *
     * @return The parent {@code Transformer} of this {@code Transformation}.
     */
    Transformer getParent();

    /**
     * Gets this {@link Transformation Transformation's} {@link Target}.
     *
     * @return The {@code Target} of this {@code Transformation}.
     */
    Target getTarget();

    /**
     * Gets this {@link Transformation Transformation's} map of named source {@link Target Targets}.
     *
     * <p>Sources are the {@link Node Nodes} that this {@code Transformation} requests as input.
     *
     * @return This {@code Transformation Transformation's} input sources
     *         as a map of string-named {@code Target targets}.
     */
    default Map<String, Target> getSources() {
        return Collections.emptyMap();
    }

    /**
     * Applies this {@link Transformation} to the given {@link Node},
     * possibly using its {@code Node} sources.
     *
     * @param targetNode A {@code Node} to apply this {@code Transformation} to.
     *
     * @param nodeSources The sources of the target {@code Node}.
     *
     * @return The {@code Node} resulting from applying this {@code Transformation}.
     */
    Node apply(Node targetNode, Map<String, Node> nodeSources);
}
