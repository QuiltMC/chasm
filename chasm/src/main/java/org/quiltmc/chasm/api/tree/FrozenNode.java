package org.quiltmc.chasm.api.tree;

/**
 * This interface marks {@link FrozenNode}s in a CHASM tree.
 *
 * <p> Frozen nodes are immutable, but can be thawed.
 */
public interface FrozenNode extends Node {
    @Override
    Node asMutable();

    default FrozenNode asImmutable() {
        return this;
    }
}
