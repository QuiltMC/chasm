package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.lang.ast.IntegerExpression;

public class ChasmIntegerNodeExpression extends IntegerExpression implements ChasmNodeExpression {
    private final WrapperValueNode node;

    public ChasmIntegerNodeExpression(WrapperValueNode node) {
        super(node.getValueAsInt());
        this.node = node;
    }

    @Override
    public WrapperValueNode getNode() {
        return node;
    }
}
