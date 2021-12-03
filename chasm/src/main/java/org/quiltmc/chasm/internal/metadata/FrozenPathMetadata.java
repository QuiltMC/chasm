/**
 *
 */
package org.quiltmc.chasm.internal.metadata;

import java.util.Collection;
import java.util.List;
import org.quiltmc.chasm.internal.tree.frozencollection.ImmutableArrayList;

/**
 *
 */
public class FrozenPathMetadata extends ImmutableArrayList<PathEntry> implements FrozenMetadata, List<PathEntry> {


    /**
     * @param pathMetadata
     */
    public FrozenPathMetadata(Collection<PathEntry> pathMetadata) {
        super(pathMetadata.toArray(new PathEntry[pathMetadata.size()]));
    }

    private FrozenPathMetadata(PathEntry[] path) {
        super(path);
    }

    @Override
    public Metadata thaw() {
        return new PathMetadata(this);
    }

    @Override
    protected FrozenPathMetadata newList(PathEntry[] path) {
        return new FrozenPathMetadata(path);
    }
}
