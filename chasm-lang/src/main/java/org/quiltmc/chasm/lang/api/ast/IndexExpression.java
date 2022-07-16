package org.quiltmc.chasm.lang.api.ast;

import java.util.Objects;

public class IndexExpression extends Expression {
    private Expression left;
    private Expression index;

    public IndexExpression(Expression left, Expression index) {
        this.left = left;
        this.index = index;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression expression) {
        this.left = expression;
    }

    public Expression getIndex() {
        return index;
    }

    public void setIndex(Expression index) {
        this.index = index;
    }

    @Override
    public IndexExpression copy() {
        return new IndexExpression(left.copy(), index.copy());
    }
}
