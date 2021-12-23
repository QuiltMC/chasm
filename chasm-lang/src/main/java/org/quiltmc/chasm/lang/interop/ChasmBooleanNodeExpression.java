package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.lang.ast.BooleanExpression;

public class ChasmBooleanNodeExpression extends BooleanExpression implements ChasmNodeExpression {
    private final WrapperValueNode node;

    public ChasmBooleanNodeExpression(WrapperValueNode node) {
        super(node.getValueAsBoolean());
        this.node = node;
    }

    @Override
    public WrapperValueNode getNode() {
        return node;
    }
}
