package org.quiltmc.chasm.lang.api.ast;

/**
 * An integer literal expression.
 */
public class IntegerNode extends ValueNode<Long> {
    /**
     * Creates an integer literal expression.
     *
     * @see Ast#literal(long)
     */
    public IntegerNode(long value) {
        super(value);
    }
}
