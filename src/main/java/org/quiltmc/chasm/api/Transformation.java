package org.quiltmc.chasm.api;

import java.util.Map;

import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.Node;

public final class Transformation {
    private final Transformer parent;
    private final Target target;
    private final Map<String, Target> sources;
    private final Function applyFunction;

    public Transformation(Transformer parent, Target target, Map<String, Target> sources, Function applyFunction) {
        this.parent = parent;
        this.target = target;
        this.sources = sources;
        this.applyFunction = applyFunction;
    }

    /**
     * A getter for this {@link Transformation}'s parent {@link Transformer}.
     * 
     * @return The parent {@link Transformer} of this {@link Transformation}.
     */
    public Transformer getParent() {
        return parent;
    }

    /**
     * A getter for this {@link Transformation}'s {@link Target}.
     * 
     * @return The {@link Target} of this {@link Transformation}.
     */
    public Target getTarget() {
        return target;
    }

    /**
     * A getter for this {@link Transformation}'s map of named source {@link Target}s.
     * 
     * @return This {@link Transformation}'s sources as a {@link Map} from name {@link String} to {@link Target} source.
     */
    public Map<String, Target> getSources() {
        return sources;
    }

    /**
     * @param targetNode A {@link Node} to apply this {@link Transformation} to.
     * 
     * @param nodeSources The sources of the target {@link Node}.
     * 
     * @return The {@link Node} resulting from applying this {@link Transformation}.
     */
    public Node apply(Node targetNode, Map<String, Node> nodeSources) {
        return applyFunction.apply(targetNode, nodeSources);
    }

    /**
     * A function that transforms the passed {@link Node}, possibly using its sources.
     */
    @FunctionalInterface
    public interface Function {
        /**
         * @param targetNode A {@link Node} to transform.
         * 
         * @param nodeSources The sources of the target {@link Node}.
         * 
         * @return The {@link Node} resulting from transforming the given {@link Node}.
         */
        Node apply(Node targetNode, Map<String, Node> nodeSources);
    }
}
