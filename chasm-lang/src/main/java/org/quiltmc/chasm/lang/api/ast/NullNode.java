package org.quiltmc.chasm.lang.api.ast;

public final class NullNode extends ValueNode<Void> {
    public static NullNode INSTANCE = new NullNode();

    private NullNode() {
        super(null);
    }
}
