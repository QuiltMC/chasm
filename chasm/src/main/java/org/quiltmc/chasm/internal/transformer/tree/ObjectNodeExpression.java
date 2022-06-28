package org.quiltmc.chasm.internal.transformer.tree;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.LiteralExpression;

public class ObjectNodeExpression extends LiteralExpression<Object> implements NodeExpression {
    private final ValueNode node;

    public ObjectNodeExpression(ParseTree tree, ValueNode node) {
        super(tree, node.getValue());
        this.node = node;
    }

    @Override
    public Node getNode() {
        return node;
    }
}
