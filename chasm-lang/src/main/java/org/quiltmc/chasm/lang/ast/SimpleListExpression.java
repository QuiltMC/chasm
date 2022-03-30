package org.quiltmc.chasm.lang.ast;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.op.Expression;

public class SimpleListExpression extends AbstractListExpression {
    private final List<Expression> entries;

    public SimpleListExpression(ParseTree tree, List<Expression> entries) {
        super(tree);
        this.entries = Collections.unmodifiableList(entries);
    }

    @Override
    public Expression get(ParseTree tree, int index) {
        return entries.get(index);
    }

    @Override
    public Iterator<Expression> iterator() {
        return entries.iterator();
    }

    @Override
    public String toString() {
        return entries.toString();
    }

    public List<Expression> getEntries() {
        return entries;
    }

    @Override
    public int getLength() {
        return entries.size();
    }
}
