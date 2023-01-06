package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.RenderUtil;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * The base class for literal expressions.
 */
public abstract class ValueNode<T> extends Node {
    private T value;

    /**
     * Creates a literal expression.
     */
    public ValueNode(T value) {
        this.value = value;
    }

    /**
     * Gets the value of the literal expression.
     */
    public T getValue() {
        return value;
    }

    /**
     * Sets the value of the literal expression.
     */
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        if (value instanceof String) {
            builder.append(RenderUtil.quotify((String) value, '"'));
        } else {
            builder.append(value);
        }
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        return this;
    }
}
