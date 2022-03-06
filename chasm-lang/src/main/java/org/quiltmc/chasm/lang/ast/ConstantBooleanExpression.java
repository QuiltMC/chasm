package org.quiltmc.chasm.lang.ast;

public class ConstantBooleanExpression extends LiteralExpression<Boolean> {
    public ConstantBooleanExpression(boolean value) {
        super(value);
    }

    @Override
    public ConstantBooleanExpression copy() {
        return new ConstantBooleanExpression(value);
    }
}
