package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Indexable;

public class IndexExpression implements Expression {
    private final Expression object;
    private final Expression index;

    public IndexExpression(Expression object, Expression index) {
        this.object = object;
        this.index = index;
    }

    @Override
    public Expression reduce(ReductionContext context) {
        Expression object = context.reduce(this.object);
        Expression index = context.reduce(this.index);

        if (object instanceof Indexable && ((Indexable) object).canIndex(index)) {
            return context.reduce(((Indexable) object).index(index));
        }

        throw new RuntimeException("Cannot index " + object.getClass().getSimpleName() + " with " + index.getClass().getSimpleName());
    }

    @Override
    public void resolve(String identifier, Expression value) {
        object.resolve(identifier, value);
        index.resolve(identifier, value);
    }

    @Override
    public IndexExpression copy() {
        return new IndexExpression(object.copy(), index.copy());
    }
}
