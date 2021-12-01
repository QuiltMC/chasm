package org.quiltmc.chasm.lang.interop;

import java.util.Iterator;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.op.Indexable;
import org.quiltmc.chasm.lang.op.Iterable;

public class ChasmListNodeExpression implements ChasmNodeExpression, Indexable, Iterable {
    private final ListNode node;

    public ChasmListNodeExpression(ListNode node) {
        this.node = node;
    }

    public ListNode getNode() {
        return node;
    }

    @Override
    public void resolve(String identifier, Expression value) {
    }

    @Override
    public Expression reduce(ReductionContext context) {
        return this;
    }

    @Override
    public ChasmListNodeExpression copy() {
        return this;
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof IntegerExpression;
    }

    @Override
    public Expression index(Expression expression) {
        return ConversionHelper.convert(node.get(((IntegerExpression) expression).getValue()));
    }

    @Override
    public Iterator<Expression> iterate() {
        Iterator<Node> iterator = node.iterator();
        return new Iterator<Expression>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Expression next() {
                return ConversionHelper.convert(iterator.next());
            }
        };
    }
}
