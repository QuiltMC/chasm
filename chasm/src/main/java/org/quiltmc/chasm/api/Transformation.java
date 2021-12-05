package org.quiltmc.chasm.api;

import java.util.Map;

import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.Node;

/**
 * Transforms a {@link Target} {@link Node} using requested {@code Node} sources.
 *
 * @see Function
 */
public final class Transformation {
    private final Transformer parent;
    private final Target target;
    private final Map<String, Target> sources;
    private final Function applyFunction;

    /**
     * Makes a new {@link Transformation} with the given
     * {@link Transformer} parent, {@link Target}, input sources, and {@link Function}.
     *
     * @param parent The {@code Transformer} parent of the new {@code Transformation}.
     *
     * @param target The {@code Target} of the new {@code Transformation}.
     *
     * @param sources The named {@code Target} input {@link Node} sources the new {@code Transformation} requires.
     *
     * @param applyFunction The {@code Node}-transforming {@code Function} of the new {@code Transformation}.
     */
    public Transformation(Transformer parent, Target target, Map<String, Target> sources, Function applyFunction) {
        this.parent = parent;
        this.target = target;
        this.sources = sources;
        this.applyFunction = applyFunction;
    }

    /**
     * Gets this {@link Transformation}'s parent {@link Transformer}.
     *
     * @return The parent {@code Transformer} of this {@code Transformation}.
     */
    public Transformer getParent() {
        return parent;
    }

    /**
     * Gets this {@link Transformation}'s {@link Target}.
     *
     * @return The {@code Target} of this {@code Transformation}.
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Gets this {@link Transformation}'s map of named source {@link Target}s.
     *
     * <p>Sources are the {@link Node}s that this {@code Transformation} requests as input.
     *
     * @return This {@code Transformation}'s input sources as a map of string-named {@code Target}s.
     */
    public Map<String, Target> getSources() {
        return sources;
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
    public Node apply(Node targetNode, Map<String, Node> nodeSources) {
        return applyFunction.apply(targetNode, nodeSources);
    }

    /**
     * Transforms the passed {@link Node}, using the given input {@code Node} sources.
     */
    @FunctionalInterface
    public interface Function {
        /**
         * Applies this {@link Function} to the passed {@link Node},
         * using the given named input {@code Node} sources.
         *
         * @param targetNode The {@code Node} to transform.
         *
         * @param nodeSources The input {@code Node} sources requested by the containing {@link Transformation}.
         *
         * @return The {@code Node} to replace the {@link Target} {@code Node}.
         */
        Node apply(Node targetNode, Map<String, Node> nodeSources);
    }
}
