/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.FrozenMetadata;
import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;

/**
 *
 */
public class FrozenValueNode<T> extends ValueNode<T> implements FrozenNode {

    /**
     * @param valueNode
     */
    public FrozenValueNode(ValueNode<T> valueNode) {
        super(valueNode.getValue(), valueNode.getMetadata().freeze());
    }

    @Override
    public ValueNode<T> asMutable() {
        return new ValueNode<>(getValue(), this.getMetadata().thaw());
    }

    @Override
    public FrozenMetadataProvider<FrozenMetadata> getMetadata() {
        return (FrozenMetadataProvider<FrozenMetadata>) super.getMetadata();
    }

}
