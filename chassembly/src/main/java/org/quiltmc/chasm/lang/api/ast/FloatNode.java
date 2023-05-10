package org.quiltmc.chasm.lang.api.ast;

/**
 * A float literal expression.
 */
public final class FloatNode extends ValueNode<Double> {
    /**
     * Creates a float literal expression.
     *
     * @see Ast#literal(double)
     */
    public FloatNode(double value) {
        super(value);
    }

    @Override
    public String typeName() {
        return "float";
    }
}
