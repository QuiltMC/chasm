package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.op.Equatable;

public class StringExpression extends LiteralExpression<String> implements Equatable {
    public StringExpression(String value) {
        super(value);
    }

    @Override
    public StringExpression copy() {
        return new StringExpression(value);
    }

    @Override
    public boolean canEquate(Expression expression) {
        return expression instanceof StringExpression;
    }

    @Override
    public Expression equate(Expression expression) {
        return new BooleanExpression(value.equals(((StringExpression) expression).getValue()));
    }
}
