package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class LiteralExpression extends Expression {
    private Object value;

    public LiteralExpression(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Expression copy() {
        return new LiteralExpression(value);
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
}
