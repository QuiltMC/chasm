package org.quiltmc.chasm.lang.api.ast;

import java.util.Objects;

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
    public String toString() {
        return String.valueOf(value);
    }
}
