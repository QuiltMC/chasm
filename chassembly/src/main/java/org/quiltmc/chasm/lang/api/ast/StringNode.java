package org.quiltmc.chasm.lang.api.ast;

/**
 * A string literal expression.
 */
public final class StringNode extends ValueNode<String> {
    /**
     * Creates a string literal expression.
     *
     * @see Ast#literal(String)
     */
    public StringNode(String value) {
        super(value);
    }
}
