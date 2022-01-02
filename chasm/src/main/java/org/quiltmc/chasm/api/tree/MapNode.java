package org.quiltmc.chasm.api.tree;

import java.util.Map;

import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;
import org.quiltmc.chasm.internal.tree.CowWrapperMapNode;

/**
 * Accesses child {@link Node}s by name.
 *
 * <p>Stores a {@code Map} of name strings to child {@code Node}s.
 */
public interface MapNode extends Node, Map<String, Node> {
    @Override
    MapNode deepCopy();

    @Override
    <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperMapNode asWrapper(
            AbstractCowWrapperNode<P, W> parent, Object key, boolean owned);
}
