package org.quiltmc.chasm.transformer;

import org.quiltmc.chasm.tree.LinkedHashMapNode;

import java.util.Collection;

public interface Transformer {
    Collection<Transformation> apply(LinkedHashMapNode classes);

    String getId();
}
