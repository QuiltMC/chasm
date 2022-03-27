package org.quiltmc.chasm.lang.ast;

import java.util.Objects;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.EquatableExpression;
import org.quiltmc.chasm.lang.op.Expression;

public abstract class LiteralExpression<T> extends AbstractExpression implements EquatableExpression {
    protected final T value;

    public LiteralExpression(ParseTree tree, T value) {
        super(tree);
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public LiteralExpression<T> resolve(ScopeStack scope) {
        return this;
    }

    @Override
    public LiteralExpression<T> reduce(Cache cache) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LiteralExpression<?> that = (LiteralExpression<?>) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean canEquate(Expression expression) {
        return expression instanceof LiteralExpression;
    }

    @Override
    public ConstantBooleanExpression equate(ParseTree tree, Expression expression) {
        return new ConstantBooleanExpression(tree, Objects.equals(value, ((LiteralExpression<?>) expression).value));
    }
}
