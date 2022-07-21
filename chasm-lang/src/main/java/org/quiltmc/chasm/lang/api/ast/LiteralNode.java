package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class LiteralNode extends Node {
    private Object value;

    public LiteralNode(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Node copy() {
        return new LiteralNode(value);
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        if (value instanceof String) {
            builder.append('"').append(((String) value).replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
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
