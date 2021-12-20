/**
 *
 */
package org.quiltmc.chasm.internal.metadata;

import org.quiltmc.chasm.api.metadata.COWWrapperMetadataProvider;
import org.quiltmc.chasm.api.metadata.CowWrapperMetadata;
import org.quiltmc.chasm.api.metadata.Metadata;
import org.quiltmc.chasm.api.util.CowWrapper;

/**
 * This feels silly.
 */
public class CowWrapperOriginMetadata extends CowWrapperMetadata<OriginMetadata> {

    /**
     * @param <M>
     * @param parent
     * @param key
     * @param object
     * @param owned
     */
    public <M extends Metadata> CowWrapperOriginMetadata(COWWrapperMetadataProvider parent, OriginMetadata object,
            boolean owned) {
        super(parent, OriginMetadata.class, object, owned);
    }

    private CowWrapperOriginMetadata(COWWrapperMetadataProvider parent, OriginMetadata object, boolean owned,
            CowWrapperOriginMetadata partener) {
        super(parent, OriginMetadata.class, object, owned);
    }

    public CowWrapperOriginMetadata(CowWrapperMetadata<OriginMetadata> other) {
        super(other);
    }

    private CowWrapperOriginMetadata(CowWrapperMetadata<OriginMetadata> other, CowWrapperOriginMetadata partener) {
        super(other);
    }

    @Override
    public CowWrapperOriginMetadata deepCopy() {
        return shallowCopy();
    }

    @Override
    public CowWrapperOriginMetadata shallowCopy() {
        return this;
    }

    @Override
    public CowWrapperOriginMetadata asWrapper(COWWrapperMetadataProvider parent, boolean owned) {
        if (this.isOwned() == owned) {
            return this;
        } else {
            CowWrapperOriginMetadata copy = new CowWrapperOriginMetadata(this);
            copy.toOwned(owned);
            return copy;
        }
    }

    @Override
    protected <C> void updateThisWrapper(Object key, CowWrapper child, C contents) {
        throw new UnsupportedOperationException();
    }

}
