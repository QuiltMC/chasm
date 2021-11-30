package org.quiltmc.chasm.internal.metadata;

import java.util.HashMap;
import java.util.Map;

public class MetadataProvider {
    private final Map<Class<? extends Metadata>, Metadata> metadata;

    public MetadataProvider() {
        this.metadata = new HashMap<>();
    }

    public <T extends Metadata> void put(Class<T> dataClass, T data) {
        metadata.put(dataClass, data);
    }

    @SuppressWarnings("unchecked")
    public <T extends Metadata> T get(Class<T> dataClass) {
        return (T) metadata.get(dataClass);
    }

    public MetadataProvider copy() {
        MetadataProvider copy = new MetadataProvider();

        for (Map.Entry<Class<? extends Metadata>, Metadata> entry : metadata.entrySet()) {
            copy.metadata.put(entry.getKey(), entry.getValue().copy());
        }

        return copy;
    }
}
