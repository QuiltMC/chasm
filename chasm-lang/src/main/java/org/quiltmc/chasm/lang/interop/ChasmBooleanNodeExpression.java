package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.BooleanExpression;

public class ChasmBooleanNodeExpression extends BooleanExpression implements ChasmNodeExpression {
    private final ValueNode<Boolean> node;

    public ChasmBooleanNodeExpression(ValueNode<Boolean> node) {
        super(node.getValue());
        this.node = node;
    }

    @Override
    public ValueNode<Boolean> getNode() {
        return node;
    }
}
