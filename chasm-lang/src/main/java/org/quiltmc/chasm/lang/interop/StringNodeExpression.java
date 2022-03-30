package org.quiltmc.chasm.lang.interop;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.StringExpression;

public class StringNodeExpression extends StringExpression implements NodeExpression {
    private final ValueNode node;

    public StringNodeExpression(ParseTree tree, ValueNode node) {
        super(tree, node.getValueAsString());
        this.node = node;
    }

    @Override
    public Node getNode() {
        return node;
    }
}
