package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.Assert;

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
        Assert.check(value != null, "Null given to StringNode");
    }

    @Override
    public String typeName() {
        return "string";
    }
}
