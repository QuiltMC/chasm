package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.lang.ast.StringExpression;

public class ChasmStringNodeExpression extends StringExpression implements ChasmNodeExpression {
    private final WrapperValueNode node;

    public ChasmStringNodeExpression(WrapperValueNode node) {
        super(node.getValueAsString());
        this.node = node;
    }

    @Override
    public WrapperValueNode getNode() {
        return node;
    }
}
