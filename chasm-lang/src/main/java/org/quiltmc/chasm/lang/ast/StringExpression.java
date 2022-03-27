package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.op.AddableExpression;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.IndexableExpression;

public class StringExpression extends LiteralExpression<String> implements AddableExpression, IndexableExpression {
    public StringExpression(ParseTree tree, String value) {
        super(tree, value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean canAdd(Expression expression) {
        return expression instanceof StringExpression;
    }

    @Override
    public Expression add(ParseTree tree, Expression expression) {
        return new StringExpression(tree, value + ((StringExpression) expression).getValue());
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression index(ParseTree tree, Expression expression) {
        int i = ((IntegerExpression) expression).getValue().intValue();
        return 0 <= i && i < value.length()
                ? new StringExpression(tree, Character.toString(value.charAt(i)))
                : new NullExpression(tree);
    }
}
