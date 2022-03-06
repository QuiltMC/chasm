package org.quiltmc.chasm.lang.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Iterable;

public class FilterExpression implements Expression {
    private final Expression list;
    private final Expression filter;

    public FilterExpression(Expression list, Expression filter) {
        this.list = list;
        this.filter = filter;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        list.resolve(identifier, value);
        filter.resolve(identifier, value);
    }

    @Override
    public Expression reduce(ReductionContext context) {
        Expression list = context.reduce(this.list);
        Expression filter = context.reduce(this.filter);

        if (list instanceof Iterable && filter instanceof FunctionExpression) {
            Iterator<Expression> iterator = ((Iterable) list).iterate();
            List<Expression> filteredEntries = new ArrayList<>();

            while (iterator.hasNext()) {
                Expression entry = iterator.next();
                Expression result = context.reduce(((FunctionExpression) filter).call(entry));
                if (result instanceof ConstantBooleanExpression) {
                    if (((ConstantBooleanExpression) result).getValue()) {
                        filteredEntries.add(context.reduce(entry));
                    }
                } else {
                    throw new RuntimeException("Result of filter function wasn't a boolean.");
                }
            }

            return new ListExpression(filteredEntries);
        }

        throw new RuntimeException("Filter operation can only apply functions to lists.");
    }

    @Override
    public FilterExpression copy() {
        return new FilterExpression(list.copy(), filter.copy());
    }
}
