package org.quiltmc.chasm.lang.ast;

import java.util.ArrayList;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.ListExpression;

public abstract class AbstractListExpression extends AbstractExpression implements ListExpression {
    public AbstractListExpression(ParseTree tree) {
        super(tree);
    }

    public abstract Expression get(ParseTree tree, int index);

    @Override
    public Expression resolve(ScopeStack scope) {
        ArrayList<Expression> newEntries = new ArrayList<>();

        for (Expression entry : this) {
            newEntries.add(entry.resolve(scope));
        }

        return new SimpleListExpression(getParseTree(), newEntries);
    }

    @Override
    public Expression reduce(Cache cache) {
        return this;
    }

    @Override
    public boolean canAdd(Expression expression) {
        return expression instanceof AbstractListExpression;
    }

    @Override
    public Expression add(ParseTree tree, Expression expression) {
        AbstractListExpression other = (AbstractListExpression) expression;
        ArrayList<Expression> newEntries = new ArrayList<>();

        for (Expression entry : this) {
            newEntries.add(entry);

        }

        for (Expression entry : other) {
            newEntries.add(entry);

        }

        return new SimpleListExpression(tree, newEntries);
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression index(ParseTree tree, Expression expression) {
        return get(tree, ((IntegerExpression) expression).getValue().intValue());
    }
}
