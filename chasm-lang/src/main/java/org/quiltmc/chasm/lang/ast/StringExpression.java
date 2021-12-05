package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.op.Indexable;

public class StringExpression extends LiteralExpression<String> implements Indexable {
    public StringExpression(String value) {
        super(value);
    }

    @Override
    public StringExpression copy() {
        return new StringExpression(value);
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression index(Expression expression) {
        int index = ((IntegerExpression) expression).getValue();
        if (index >= value.length()) {
            return Expression.none();
        }

        return new StringExpression(String.valueOf(value.charAt(index)));
    }
}
