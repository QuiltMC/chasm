package org.quiltmc.chasm.lang.ast;

public class NoneExpression extends LiteralExpression<Void> {
    public static NoneExpression NONE = new NoneExpression();

    private NoneExpression() {
        super(null);
    }

    @Override
    public NoneExpression copy() {
        return NONE;
    }
}
