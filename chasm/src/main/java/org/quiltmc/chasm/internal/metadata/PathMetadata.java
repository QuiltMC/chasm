/**
 *
 */
package org.quiltmc.chasm.internal.metadata;

import java.util.List;

import org.quiltmc.chasm.api.metadata.CowWrapperMetadataProvider;
import org.quiltmc.chasm.api.metadata.Metadata;
import org.quiltmc.chasm.api.tree.Node;

/**
 *
 */
public interface PathMetadata extends Metadata, List<ListPathMetadata.Entry> {

    @Override
    PathMetadata deepCopy();

    PathMetadata append(String name);

    PathMetadata append(int index);

    PathMetadata parent();

    boolean startsWith(PathMetadata other);

    Node resolve(Node root);

    @Override
    <T extends Metadata> T asWrapper(CowWrapperMetadataProvider parent, Class<T> key,
            boolean owned);

}
