package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.IntegerExpression;

public class ChasmIntegerNodeExpression extends IntegerExpression implements ChasmNodeExpression {
    private final ValueNode<Integer> node;

    public ChasmIntegerNodeExpression(ValueNode<Integer> node) {
        super(node.getValue());
        this.node = node;
    }

    @Override
    public ValueNode<Integer> getNode() {
        return node;
    }
}
