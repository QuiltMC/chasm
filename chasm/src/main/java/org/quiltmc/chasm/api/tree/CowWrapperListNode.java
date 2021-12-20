/**
 *
 */
package org.quiltmc.chasm.api.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.util.UpdatableCowWrapper;

/**
 *
 */
public class CowWrapperListNode extends CowWrapperNode<ListNode, CowWrapperListNode> implements ListNode {

    /**
     * @param object
     * @param owned
     * @param parent
     */
    public <K extends Object> CowWrapperListNode(UpdatableCowWrapper parent, K key, ListNode object, boolean owned) {
        super(parent, key, object, owned);
    }

    protected CowWrapperListNode(CowWrapperListNode cowWrapperListNode) {
        super(cowWrapperListNode);
    }

    @Override
    public Node shallowCopy() {
        return new CowWrapperListNode(this);
    }

    @Override
    public <P extends Node, W extends CowWrapperNode<P, W>> CowWrapperListNode asWrapper(CowWrapperNode<P, W> parent,
            Object key, boolean owned) {
        if (this.checkParentLink(parent)) {
            if (this.isOwned() == owned) {
                return this;
            } else {
                CowWrapperListNode copy = new CowWrapperListNode(this);
                copy.toOwned(owned);
                return copy;
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public int size() {
        return this.object.size();
    }

    @Override
    public boolean isEmpty() { return this.object.isEmpty(); }

    @Override
    public boolean contains(Object o) {
        return this.object.contains(o);
    }

    @Override
    public Iterator<Node> iterator() {

    }

    @Override
    public Object[] toArray() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean add(Node e) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean remove(Object o) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Node> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    public Node get(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node set(int index, Node element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void add(int index, Node element) {
        // TODO Auto-generated method stub

    }

    @Override
    public Node remove(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int indexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int lastIndexOf(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ListIterator<Node> listIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ListIterator<Node> listIterator(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Node> subList(int fromIndex, int toIndex) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CowWrapperListNode castThis() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CowWrapperListNode deepCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected <C> void updateThisWrapper(Object key, CowWrapper child, C contents) {
        // TODO Auto-generated method stub

    }

}
