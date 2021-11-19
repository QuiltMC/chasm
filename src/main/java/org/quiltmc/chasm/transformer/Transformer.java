package org.quiltmc.chasm.transformer;

import java.util.Collection;

import org.quiltmc.chasm.tree.ListNode;

public interface Transformer {
    Collection<Transformation> apply(ListNode classes);

    String getId();
}
