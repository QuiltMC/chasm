package org.quiltmc.chasm.lang.ast;

import java.util.Objects;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Equatable;

public abstract class LiteralExpression<T> implements Expression, Equatable {
    protected final T value;

    protected LiteralExpression(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    @Override
    public void resolve(String identifier, Expression value) {
    }

    @Override
    public Expression reduce(ReductionContext context) {
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
    public boolean canEquate(Expression expression) {
        return expression instanceof LiteralExpression;
    }

    @Override
    public Expression equate(Expression expression) {
        return new ConstantBooleanExpression(Objects.equals(value, ((LiteralExpression<?>) expression).value));
    }
}
