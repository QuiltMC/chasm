package org.quiltmc.chasm.api;

import java.util.Collection;

import org.quiltmc.chasm.api.tree.ListNode;

public interface Transformer {
    Collection<Transformation> apply(ListNode classes);

    String getId();

    default boolean mustRunAfter(String transformerId) {
        return false;
    }

    default boolean mustRunBefore(String transformerId) {
        return false;
    }

    default boolean mustRunRoundAfter(String transformerId) {
        return false;
    }

    default boolean mustRunRoundBefore(String transformerId) {
        return false;
    }
}
