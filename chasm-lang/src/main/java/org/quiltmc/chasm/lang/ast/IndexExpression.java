package org.quiltmc.chasm.lang.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.FunctionExpression;
import org.quiltmc.chasm.lang.op.IndexableExpression;
import org.quiltmc.chasm.lang.op.IterableExpression;

public class IndexExpression extends AbstractExpression {
    private final Expression object;
    private final Expression index;

    public IndexExpression(ParseTree tree, Expression object, Expression index) {
        super(tree);
        this.object = object;
        this.index = index;
    }

    @Override
    public Expression resolve(ScopeStack scope) {
        return new IndexExpression(getParseTree(), object.resolve(scope), index.resolve(scope));
    }

    @Override
    public Expression reduce(Cache cache) {
        Expression object = cache.reduceCached(this.object);
        Expression index = cache.reduceCached(this.index);

        if (object instanceof IndexableExpression && ((IndexableExpression) object).canIndex(index)) {
            Expression entry = ((IndexableExpression) object).index(getParseTree(), index);
            return cache.reduceCached(entry);
        } else if (object instanceof IterableExpression && index instanceof FunctionExpression) {
            List<Expression> filteredEntries = new ArrayList<>();
            for (Expression entry : (IterableExpression) object) {
                CallExpression call = new CallExpression(getParseTree(), index, entry);
                Expression callResult = cache.reduceCached(call);
                if (callResult instanceof ConstantBooleanExpression) {
                    if (((ConstantBooleanExpression) callResult).getValue()) {
                        filteredEntries.add(entry);
                    }
                } else {
                    //TODO: Proper Error
                    throw new RuntimeException("Filter function must return boolean");
                }
            }
            return new SimpleListExpression(getParseTree(), filteredEntries);
        } else {
            // TODO: Proper Error
            throw new RuntimeException(
                    "Cannot index " + object.getClass().getSimpleName() + " with " + index.getClass().getSimpleName());
        }
    }
}
