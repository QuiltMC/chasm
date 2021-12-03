package org.quiltmc.chasm.api;

import java.util.Map;

import org.quiltmc.chasm.api.target.Target;
import org.quiltmc.chasm.api.tree.FrozenNode;
import org.quiltmc.chasm.api.tree.Node;

/**
 * Transform a {@link Target} {@link Node} using the {@link Node}'s sources, and a {@link Function}.
 */
public final class Transformation {
    private final Transformer parent;
    private final Target target;
    private final Map<String, Target> sources;
    private final Function applyFunction;

    /**
     * Make a new {@link Transformation} with the given
     *  {@link Transformer} parent, {@link Target}, {@link Target} sources, and {@link Function}.
     *
     * @param parent The {@link Transformer} parent of the new {@link Transformation}.
     *
     * @param target The {@link Target} of the new {@link Transformation}.
     *
     * @param sources The named {@link Target} sources of the new {@link Transformation}.
     *
     * @param applyFunction The {@link Node}-transforming {@link Function} of the new {@link Transformation}.
     */
    public Transformation(Transformer parent, Target target, Map<String, Target> sources, Function applyFunction) {
        this.parent = parent;
        this.target = target;
        this.sources = sources;
        this.applyFunction = applyFunction;
    }

    /**
     * Get this {@link Transformation}'s parent {@link Transformer}.
     *
     * @return The parent {@link Transformer} of this {@link Transformation}.
     */
    public Transformer getParent() {
        return parent;
    }

    /**
     * Get this {@link Transformation}'s {@link Target}.
     *
     * @return The {@link Target} of this {@link Transformation}.
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Get this {@link Transformation}'s map of named source {@link Target}s.
     *
     * @return This {@link Transformation}'s sources as a {@link Map} from name {@link String} to {@link Target} source.
     */
    public Map<String, Target> getSources() {
        return sources;
    }

    /**
     * Apply this {@link Transformation} to the given {@link Node},
     *                 possibly using its {@link Node} sources.
     *
     * @param targetNode A {@link Node} to apply this {@link Transformation} to.
     *
     * @param nodeSources The sources of the target {@link Node}.
     *
     * @return The {@link Node} resulting from applying this {@link Transformation}.
     */
    public FrozenNode apply(FrozenNode targetNode, Map<String, ? extends FrozenNode> nodeSources) {
        return applyFunction.apply(targetNode, nodeSources);
    }

    /**
     * Transform the passed {@link Node}, possibly using its sources.
     */
    @FunctionalInterface
    public interface Function {
        /**
         * Apply this {@link Function} to the given {@link Node},
         *              possibly using its {@link Node} sources.
         *
         * @param targetNode A {@link Node} to transform.
         *
         * @param nodeSources The sources of the target {@link Node}.
         *
         * @return The {@link Node} resulting from transforming the given {@link Node}.
         */
        FrozenNode apply(FrozenNode targetNode, Map<String, ? extends FrozenNode> nodeSources);
    }
}
