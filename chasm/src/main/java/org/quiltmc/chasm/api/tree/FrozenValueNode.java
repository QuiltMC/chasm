/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;

/**
 *
 */
public class FrozenValueNode extends ValueNode implements FrozenNode {

    /**
     * @param valueNode
     */
    public FrozenValueNode(ValueNode valueNode) {
        super(valueNode.getValue(), valueNode.getMetadata().freeze());
    }

    @Override
    public ValueNode asMutable() {
        return new ValueNode(getValue(), getMetadata().thaw());
    }

    @Override
    public FrozenMetadataProvider getMetadata() {
        return (FrozenMetadataProvider) super.getMetadata();
    }

    @Override
    public FrozenValueNode asImmutable() {
        return this;
    }

    @Override
    public FrozenMetadataProvider getFrozenMetadata() {
        return (FrozenMetadataProvider) (Object) super.getMetadata();
    }
}
