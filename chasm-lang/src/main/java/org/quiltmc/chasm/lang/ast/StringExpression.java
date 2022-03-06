package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.op.Addable;
import org.quiltmc.chasm.lang.op.Indexable;

public class StringExpression extends LiteralExpression<String> implements Indexable, Addable {
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

    @Override
    public boolean canAdd(Expression expression) {
        return expression instanceof StringExpression;
    }

    @Override
    public Expression add(Expression expression) {
        return new StringExpression(value + ((StringExpression) expression).getValue());
    }
}
