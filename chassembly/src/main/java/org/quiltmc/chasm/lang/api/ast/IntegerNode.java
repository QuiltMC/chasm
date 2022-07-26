package org.quiltmc.chasm.lang.api.ast;

public class IntegerNode extends ValueNode<Long> {
    public IntegerNode(long value) {
        super(value);
    }

    public IntegerNode(int value) {
        super((long) value);
    }
}
