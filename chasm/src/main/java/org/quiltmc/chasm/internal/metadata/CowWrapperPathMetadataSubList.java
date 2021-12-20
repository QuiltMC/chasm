/**
 *
 */
package org.quiltmc.chasm.internal.metadata;

import org.quiltmc.chasm.api.metadata.COWWrapperMetadataProvider;
import org.quiltmc.chasm.api.metadata.Metadata;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.ListPathMetadata.Entry;
import org.quiltmc.chasm.internal.util.ListWrapperSubList;

class CowWrapperPathMetadataSubList extends ListWrapperSubList<Entry, CowWrapperPathMetadata> implements PathMetadata {
    /**
     * @param cowWrapperPathMetadata
     * @param fromIndex
     * @param toIndex
     */
    public CowWrapperPathMetadataSubList(CowWrapperPathMetadata cowWrapperPathMetadata, int fromIndex, int toIndex) {
        super(cowWrapperPathMetadata);
        if (fromIndex > toIndex) {
            // reverse the range
            // +1s are because its [fromIndex, toIndex)
            int tempMin = toIndex + 1;
            toIndex = fromIndex + 1;
            fromIndex = tempMin;
        }
        if (toIndex > cowWrapperPathMetadata.size()) {
            toIndex = cowWrapperPathMetadata.size();
        }
        this.min = fromIndex;
        this.max = toIndex;
    }

    @Override
    public CowWrapperPathMetadataSubList deepCopy() {
        return new CowWrapperPathMetadataSubList(list.deepCopy(), min, max);
    }

    @Override
    public CowWrapperPathMetadataSubList shallowCopy() {
        return new CowWrapperPathMetadataSubList(list, min, max);
    }

    @Override
    public PathMetadata append(String name) {
        PathMetadata pmi = new ListPathMetadata();
        pmi.addAll(this);
        pmi.add(new Entry(name));
        return pmi;
    }

    @Override
    public PathMetadata append(int index) {
        PathMetadata pmi = new ListPathMetadata();
        pmi.addAll(this);
        pmi.add(new Entry(index));
        return pmi;
    }

    @Override
    public PathMetadata parent() {
        PathMetadata pmi = new ListPathMetadata();
        for (int i = this.min; i < this.max - 1; ++i) {
            pmi.add(this.list.get(i));
        }
        return pmi;
    }

    @Override
    public boolean startsWith(PathMetadata other) {
        if (other.size() > this.size()) {
            return false;
        }
        for (int i = 0; i < other.size(); ++i) {
            if (!this.list.get(i + this.min).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Node resolve(Node root) {
        for (Entry entry : this) {
            if (entry.isInteger()) {
                ListNode list = (ListNode) root;
                root = list.get(entry.asInteger());
            } else if (entry.isString()) {
                MapNode map = (MapNode) root;
                root = map.get(entry.asString());
            } else {
                throw new UnsupportedOperationException("Can't apply given list to node.");
            }
        }
        return root;
    }

    @Override
    public <T extends Metadata> T asWrapper(COWWrapperMetadataProvider parent, Class<T> key, boolean owned) {
        // Why would you do this though?
        return key.cast(new CowWrapperPathMetadata(parent, this, owned));
    }

}