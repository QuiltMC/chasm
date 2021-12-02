package org.quiltmc.chasm.lang.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Addable;
import org.quiltmc.chasm.lang.op.Indexable;
import org.quiltmc.chasm.lang.op.Iterable;

public class ListExpression implements Expression, Indexable, Iterable, Addable {
    private final List<Expression> entries;

    public ListExpression(List<Expression> entries) {
        this.entries = entries;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        for (Expression entry : entries) {
            entry.resolve(identifier, value);
        }
    }

    @Override
    public ListExpression reduce(ReductionContext context) {
        List<Expression> reduced = new ArrayList<>();
        for (Expression entry : entries) {
            reduced.add(context.reduce(entry));
        }
        return new ListExpression(reduced);
    }

    @Override
    public ListExpression copy() {
        List<Expression> copies = new ArrayList<>();

        for (Expression entry : entries) {
            copies.add(entry.copy());
        }

        return new ListExpression(copies);
    }

    public Expression get(int index) {
        return 0 <= index && index < entries.size() ? entries.get(index) : Expression.none();
    }

    public List<Expression> getEntries() {
        return entries;
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression index(Expression expression) {
        return get(((IntegerExpression) expression).getValue());
    }

    @Override
    public Iterator<Expression> iterate() {
        return entries.iterator();
    }

    @Override
    public boolean canAdd(Expression expression) {
        return expression instanceof ListExpression;
    }

    @Override
    public Expression add(Expression expression) {
        List<Expression> newEntries = new ArrayList<>(entries);
        newEntries.addAll(((ListExpression) expression).entries);
        return new ListExpression(newEntries);
    }
}
