package org.quiltmc.chasm.internal.metadata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @param <F>
 *
 */
public class FrozenMetadataProvider extends MetadataProvider<FrozenMetadata> {
    /**
     * @param metadataProvider
     */
    public FrozenMetadataProvider(Map<Class<? extends FrozenMetadata>, FrozenMetadata> map) {
        super(map);
    }

    @Override
    public <T extends FrozenMetadata> void put(T data) {
        throw new UnsupportedOperationException("FrozenMetadataProviders are immutable.");
    }

    @Override
    public FrozenMetadataProvider freeze() {
        // Already frozen
        return this;
    }

    @Override
    public MetadataProvider<Metadata> thaw() {
        Map<Class<? extends Metadata>, Metadata> thawingMap = new HashMap<>(size());
        Iterator<FrozenMetadata> metaIter = iterator();
        for (FrozenMetadata frozenData = metaIter.next(); metaIter.hasNext(); frozenData = metaIter.next()) {
            Metadata data = frozenData.thaw();
            thawingMap.put(data.getClass(), data);
        }

        return new MetadataProvider<>(thawingMap);
    }
}
