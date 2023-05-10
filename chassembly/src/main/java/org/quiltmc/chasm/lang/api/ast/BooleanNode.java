package org.quiltmc.chasm.lang.api.ast;

/**
 * A boolean literal expression.
 */
public final class BooleanNode extends ValueNode<Boolean> {
    /**
     * The boolean literal expression representing {@code true}.
     */
    public static final BooleanNode TRUE = new BooleanNode(true);
    /**
     * The boolean literal expression representing {@code false}.
     */
    public static final BooleanNode FALSE = new BooleanNode(false);

    private BooleanNode(Boolean value) {
        super(value);
    }

    /**
     * Returns the boolean literal expression representing the given value.
     *
     * @see Ast#literal(boolean)
     */
    public static BooleanNode from(boolean value) {
        return value ? TRUE : FALSE;
    }
}
