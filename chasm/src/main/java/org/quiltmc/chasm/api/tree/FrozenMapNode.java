/**
 *
 */
package org.quiltmc.chasm.api.tree;

/**
 *
 */
public interface FrozenMapNode extends FrozenNode, MapNode<FrozenNode> {
    @Override
    default FrozenMapNode asImmutable() {
        return this;
    }

    @Override
    MapNode<Node> asMutable();
}
