package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;

public class NullExpression extends LiteralExpression<Void> {
    public NullExpression(ParseTree tree) {
        super(tree, null);
    }
}
