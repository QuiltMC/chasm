package org.quiltmc.chasm.internal.metadata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @param <F>
 *
 */
public class FrozenMetadataProvider<F extends FrozenMetadata> extends MetadataProvider<F> {
    /**
     * @param metadataProvider
     */
    public FrozenMetadataProvider(Map<Class<? extends F>, F> map) {
        super(map);
    }

    @Override
    public <T extends F> void put(T data) {
        throw new UnsupportedOperationException("FrozenMetadataProviders are immutable.");
    }

    @Override
    @SuppressWarnings("unchecked")
    public FrozenMetadataProvider<FrozenMetadata> freeze() {
        // Already frozen
        return (FrozenMetadataProvider<FrozenMetadata>) this;
    }

    @Override
    public MetadataProvider<Metadata> thaw() {
        Map<Class<? extends Metadata>, Metadata> thawingMap = new HashMap<>(size());
        Iterator<F> metaIter = iterator();
        for (F frozenData = metaIter.next(); metaIter.hasNext(); frozenData = metaIter.next()) {
            Metadata data = frozenData.thaw();
            thawingMap.put(data.getClass(), data);
        }

        return new MetadataProvider<>(thawingMap);
    }
}
