package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.lang.ast.StringExpression;

public class ChasmStringNodeExpression extends StringExpression implements ChasmNodeExpression {
    private final ValueNode<String> node;

    public ChasmStringNodeExpression(ValueNode<String> node) {
        super(node.getValue());
        this.node = node;
    }

    @Override
    public ValueNode<String> getNode() {
        return node;
    }
}
