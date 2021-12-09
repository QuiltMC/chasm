package org.quiltmc.chasm.internal.metadata;

import org.quiltmc.chasm.api.metadata.FrozenMetadata;
import org.quiltmc.chasm.api.metadata.Metadata;
import org.quiltmc.chasm.api.metadata.MetadataProvider;

/**
 * @param <F>
 *
 */
public class FrozenMetadataProvider extends MetadataProvider {
    /**
     * @param metadataProvider
     */
    public FrozenMetadataProvider(MetadataProvider metadataProvider) {
        super(metadataProvider.size());
        for (Class<? extends Metadata> clazz : metadataProvider) {
            FrozenMetadata frozenData = metadataProvider.get(clazz).freeze();
            super.putRaw(clazz, frozenData);
        }
    }

    @Override
    public <T extends Metadata> void put(Class<? super T> dataClass, T data) {
        throw new UnsupportedOperationException("FrozenMetadataProviders are immutable.");
    }

    @Override
    public FrozenMetadataProvider freeze() {
        // Already frozen
        return this;
    }

    @Override
    public MetadataProvider thaw() {
        return new MetadataProvider(this);
    }
}
