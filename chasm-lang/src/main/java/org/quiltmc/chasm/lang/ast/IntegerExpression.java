package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.op.Addable;
import org.quiltmc.chasm.lang.op.Equatable;
import org.quiltmc.chasm.lang.op.Subtractable;

public class IntegerExpression extends LiteralExpression<Integer> implements Addable, Subtractable {
    public IntegerExpression(int value) {
        super(value);
    }

    @Override
    public IntegerExpression copy() {
        return new IntegerExpression(value);
    }

    @Override
    public boolean canAdd(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression add(Expression expression) {
        int result = value + ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }

    @Override
    public boolean canSubtract(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression subtract(Expression expression) {
        int result = value - ((IntegerExpression) expression).value;
        return new IntegerExpression(result);
    }
}
