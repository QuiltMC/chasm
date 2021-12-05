package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.IntegerExpression;

public class ChasmIntegerNodeExpression extends IntegerExpression implements ChasmNodeExpression {
    private final ValueNode node;

    public ChasmIntegerNodeExpression(ValueNode node) {
        super(node.getValueAsInt());
        this.node = node;
    }

    @Override
    public ValueNode getNode() {
        return node;
    }
}
