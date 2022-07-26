package org.quiltmc.chasm.lang.api.ast;

public final class BooleanNode extends ValueNode<Boolean> {
    public static final BooleanNode TRUE = new BooleanNode(true);
    public static final BooleanNode FALSE = new BooleanNode(false);

    private BooleanNode(Boolean value) {
        super(value);
    }

    public static BooleanNode from(boolean value) {
        return value ? TRUE : FALSE;
    }
}
