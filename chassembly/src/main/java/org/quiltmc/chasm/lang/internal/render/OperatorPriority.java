package org.quiltmc.chasm.lang.internal.render;

public enum OperatorPriority {
    ANY,
    BOOLEAN_OR, BOOLEAN_AND,
    BITWISE_OR, BITWISE_XOR, BITWISE_AND,
    EQUALITY, RELATIONAL,
    SHIFT, ADDITION, MULTIPLICATIVE,
    UNARY,
    ARGUMENT_PRIMARY;


    private static final OperatorPriority[] VALUES = values();

    public boolean allowedFor(OperatorPriority minPriority) {
        return minPriority.ordinal() <= this.ordinal();
    }

    public OperatorPriority inc() {
        if (this == ARGUMENT_PRIMARY) {
            return this;
        }
        return VALUES[this.ordinal() + 1];
    }
}
