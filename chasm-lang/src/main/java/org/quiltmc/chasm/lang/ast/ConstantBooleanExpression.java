package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;

public class ConstantBooleanExpression extends LiteralExpression<Boolean> {
    public ConstantBooleanExpression(ParseTree tree, boolean value) {
        super(tree, value);
    }
}
