package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public abstract class ValueNode<T> extends Node {
    private T value;

    public ValueNode(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int indentation, OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.ARGUMENT_PRIMARY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }
        if (value instanceof String) {
            builder.append('"').append(((String) value).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
        } else {
            builder.append(value);
        }
        if (needsBrackets) {
            builder.append(')');
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
