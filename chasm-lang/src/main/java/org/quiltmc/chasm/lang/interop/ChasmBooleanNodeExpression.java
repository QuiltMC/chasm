package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.BooleanExpression;

public class ChasmBooleanNodeExpression extends BooleanExpression implements ChasmNodeExpression {
    private final ValueNode node;

    public ChasmBooleanNodeExpression(ValueNode node) {
        super(node.getValueAsBoolean());
        this.node = node;
    }

    @Override
    public ValueNode getNode() {
        return node;
    }
}
