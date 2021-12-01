package org.quiltmc.chasm.lang.interop;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.lang.ast.Expression;

public interface ChasmNodeExpression extends Expression {
    Node getNode();
}
