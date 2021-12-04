/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;
import org.quiltmc.chasm.internal.metadata.Metadata;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

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
        return new ValueNode<>(getValue(), getMetadata().thaw());
    }

    @Override
    public MetadataProvider<Metadata> getMetadata() {
        return super.getMetadata();
    }

    @Override
    public FrozenValueNode<T> asImmutable() {
        return this;
    }

    @Override
    public FrozenMetadataProvider getFrozenMetadata() {
        return (FrozenMetadataProvider) (Object) super.getMetadata();
    }
}
