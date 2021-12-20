/**
 *
 */
package org.quiltmc.chasm.api.tree;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.util.ListWrapperListIterator;
import org.quiltmc.chasm.internal.util.ListWrapperSubList;
import org.quiltmc.chasm.internal.util.ReadOnlyListWrapperIterator;

/**
 *
 */
public class CowWrapperListNode extends CowWrapperNode<ListNode, CowWrapperListNode> implements ListNode {

    /**
     * @param object
     * @param owned
     * @param parent
     */
    public <P extends Node, W extends CowWrapperNode<P, W>, K extends Object> CowWrapperListNode(
            CowWrapperNode<P, W> parent, K key, ListNode object, boolean owned) {
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
        return new ReadOnlyListWrapperIterator<>(this);
    }

    @Override
    public Object[] toArray() {
        return this.object.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.object.toArray(a);
    }

    @Override
    public boolean add(Node e) {
        this.toOwned();
        return this.object.add(e);
    }

    @Override
    public boolean remove(Object o) {
        this.toOwned();
        return this.object.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.object.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Node> c) {
        this.toOwned();
        return this.object.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Node> c) {
        this.toOwned();
        return this.object.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.toOwned();
        return this.object.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.toOwned();
        return this.object.retainAll(c);
    }

    @Override
    public void clear() {
        this.toOwned();
        this.object.clear();
    }

    @Override
    public Node get(int index) {
        return this.object.get(index);
    }

    @Override
    public Node set(int index, Node element) {
        this.toOwned();
        return this.object.get(index);
    }

    @Override
    public void add(int index, Node element) {
        this.toOwned();
        this.object.add(index, element);
    }

    @Override
    public Node remove(int index) {
        this.toOwned();
        return this.object.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.object.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.object.lastIndexOf(o);
    }

    @Override
    public ListIterator<Node> listIterator() {
        return new ListWrapperListIterator<>(this);
    }

    @Override
    public ListIterator<Node> listIterator(int index) {
        return new ListWrapperListIterator<>(this, index);
    }

    @Override
    public List<Node> subList(int fromIndex, int toIndex) {
        return new ListWrapperSubList<>(this, fromIndex, toIndex);
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
