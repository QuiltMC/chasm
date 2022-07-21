package org.quiltmc.chasm.internal.transformer.tree;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.ConstantBooleanExpression;

public class BooleanNodeNode extends ConstantBooleanExpression implements NodeNode {
    private final ValueNode node;

    public BooleanNodeNode(ParseTree tree, ValueNode node) {
        super(tree, node.getValueAsBoolean());
        this.node = node;
    }

    @Override
    public Node getNode() {
        return node;
    }
}
