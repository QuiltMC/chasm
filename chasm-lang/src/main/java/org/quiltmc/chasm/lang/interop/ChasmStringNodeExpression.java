package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.StringExpression;

public class ChasmStringNodeExpression extends StringExpression implements ChasmNodeExpression {
    private final ValueNode node;

    public ChasmStringNodeExpression(ValueNode node) {
        super(node.getValueAsString());
        this.node = node;
    }

    @Override
    public ValueNode getNode() {
        return node;
    }
}
