package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.ReductionContext;

public class ReferenceExpression implements Expression {
    private final String identifier;
    private Expression value = null;

    public ReferenceExpression(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        if (this.identifier.equals(identifier)) {
            this.value = value;
        }
    }

    @Override
    public Expression reduce(ReductionContext context) {
        if (value == null) {
            return this;
        }

        return context.reduce(value);
    }

    @Override
    public ReferenceExpression copy() {
        ReferenceExpression copy = new ReferenceExpression(identifier);
        copy.value = this.value;
        return copy;
    }
}
