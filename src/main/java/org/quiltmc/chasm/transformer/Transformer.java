package org.quiltmc.chasm.transformer;

import java.util.Collection;
import org.quiltmc.chasm.tree.LinkedHashMapNode;

public interface Transformer {
    Collection<Transformation> apply(LinkedHashMapNode classes);

    String getId();
}
