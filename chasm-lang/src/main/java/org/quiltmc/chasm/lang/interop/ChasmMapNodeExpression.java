package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.StringExpression;
import org.quiltmc.chasm.lang.op.Indexable;

public class ChasmMapNodeExpression implements ChasmNodeExpression, Indexable {
    private final MapNode node;

    public ChasmMapNodeExpression(MapNode node) {
        this.node = node;
    }

    public MapNode getNode() {
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
    public ChasmMapNodeExpression copy() {
        return this;
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof StringExpression;
    }

    @Override
    public Expression index(Expression expression) {
        return ConversionHelper.convert(node.get(((StringExpression) expression).getValue()));
    }
}
