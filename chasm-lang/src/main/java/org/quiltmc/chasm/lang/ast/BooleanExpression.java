package org.quiltmc.chasm.lang.ast;

public class BooleanExpression extends LiteralExpression<Boolean> {
    public BooleanExpression(boolean value) {
        super(value);
    }

    @Override
    public BooleanExpression copy() {
        return new BooleanExpression(value);
    }
}
