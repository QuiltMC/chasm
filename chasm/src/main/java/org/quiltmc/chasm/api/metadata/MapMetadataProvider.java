package org.quiltmc.chasm.api.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides {@link Metadata} attached to a {@link org.quiltmc.chasm.api.tree.Node}.
 */
public class MapMetadataProvider implements MetadataProvider {
    private final Map<Class<? extends Metadata>, Metadata> metadata;

    /**
     * Create a new, empty {@link MapMetadataProvider}.
     */
    public MapMetadataProvider() {
        super();
        this.metadata = new HashMap<>();
    }

    private MapMetadataProvider(MapMetadataProvider other) {
        super();
        this.metadata = new HashMap<>(other.metadata);
    }

    @Override
    public <T extends Metadata> void put(Class<T> dataClass, T data) {
        this.metadata.put(dataClass, data);
    }

    @Override
    public <T extends Metadata> T get(Class<T> dataClass) {
        return dataClass.cast(get(dataClass));
    }

    @Override
    public MapMetadataProvider deepCopy() {
        MapMetadataProvider metadataMap = new MapMetadataProvider(this);
        for (Map.Entry<Class<? extends Metadata>, Metadata> entry : metadataMap.metadata.entrySet()) {
            this.metadata.put(entry.getKey(), entry.getValue().deepCopy());
        }
        return metadataMap;
    }

    @Override
    public MapMetadataProvider shallowCopy() {
        return new MapMetadataProvider(this);
    }
}
