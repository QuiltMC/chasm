package org.quiltmc.chasm.lang.api.ast;

/**
 * A null literal expression.
 */
public final class NullNode extends ValueNode<Void> {
    /**
     * The expression representing {@code null}.
     *
     * @see Ast#nullNode()
     */
    public static final NullNode INSTANCE = new NullNode();

    private NullNode() {
        super(null);
    }

    @Override
    public String typeName() {
        return "null";
    }
}
