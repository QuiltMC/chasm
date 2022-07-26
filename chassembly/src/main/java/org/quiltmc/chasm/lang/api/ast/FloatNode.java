package org.quiltmc.chasm.lang.api.ast;

public final class FloatNode extends ValueNode<Double> {
    public FloatNode(double value) {
        super(value);
    }

    public FloatNode(float value) {
        super((double) value);
    }
}
