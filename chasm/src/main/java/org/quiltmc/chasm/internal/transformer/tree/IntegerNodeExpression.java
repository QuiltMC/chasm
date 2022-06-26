package org.quiltmc.chasm.internal.transformer.tree;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.IntegerExpression;

public class IntegerNodeExpression extends IntegerExpression implements NodeExpression {
    private final ValueNode node;

    public IntegerNodeExpression(ParseTree tree, ValueNode node) {
        super(tree, node.getValueAsInt());
        this.node = node;
    }

    @Override
    public Node getNode() {
        return node;
    }
}
