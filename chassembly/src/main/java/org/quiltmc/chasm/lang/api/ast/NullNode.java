package org.quiltmc.chasm.lang.api.ast;

public final class NullNode extends ValueNode<Void> {
    public static NullNode INSTANCE = new NullNode();

    private NullNode() {
        super(null);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " @" + Integer.toHexString(this.hashCode());
    }
}
