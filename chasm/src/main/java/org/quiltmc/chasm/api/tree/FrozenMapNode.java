/**
 *
 */
package org.quiltmc.chasm.api.tree;

/**
 *
 */
public interface FrozenMapNode extends FrozenNode, MapNode {
    @Override
    default FrozenMapNode asImmutable() {
        return this;
    }

    @Override
    MapNode asMutable();
}
