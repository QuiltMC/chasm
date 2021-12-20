/**
 *
 */
package org.quiltmc.chasm.internal.util;

import org.quiltmc.chasm.api.util.CowWrapper;

/**
 * @param <T>
 * @param <W>
 * @param <P>
 */
public abstract class AbstractCowWrapper<T extends Copyable, W extends AbstractCowWrapper<T, W>>
        implements UpdatableCowWrapper {

    protected T object;
    protected CowState state;

    protected AbstractCowWrapper(AbstractCowWrapper<T, W> other) {
        this(other.object, other.isOwned());
    }

    protected AbstractCowWrapper(T object, boolean owned) {
        super();
        this.object = object;
        if (owned) {
            this.state = CowState.OWNED;
        } else {
            this.state = CowState.SHARED;
        }
    }

    private enum CowState {
            SHARED, OWNED
    }

    @Override
    public boolean isOwned() { return this.state == CowState.OWNED; }

    protected abstract W castThis();

    public abstract W deepCopy();

    public final W getShared() {
        if (this.state == CowState.SHARED) {
            return castThis();
        }
        W newWrapper = this.deepCopy();
        newWrapper.toShared();
        return newWrapper;
    }

    public final W getOwned() {
        if (this.state == CowState.OWNED) {
            return castThis();
        }
        W newWrapper = this.deepCopy();
        newWrapper.toOwned();
        return newWrapper;
    }

    @Override
    public final boolean toShared() {
        if (this.state == CowState.SHARED) {
            return false;
        } else {
            this.state = CowState.SHARED;
            return true;
        }
    }

    @Override
    public final boolean toOwned() {
        if (this.state == CowState.SHARED) {
            copyToOwn();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected void copyToOwn() {
        this.object = (T) object.deepCopy();
        this.state = CowState.OWNED;
    }

    @Override
    public final boolean toOwned(boolean owned) {
        //return this.isOwned() != owned || (owned && this.toOwned()) || (!owned && this.toShared());
        if (this.isOwned() == owned) {
            return false;
        } else if (owned) {
            return this.toOwned();
        } else {
            return this.toShared();
        }
    }

    @Override
    public final boolean wrapsObject(Object o) {
        return this.object == o;
    }

    @Override
    public final <C extends Object> boolean updateParentWrapper(Object key, CowWrapper child, C contents) {
        final boolean updatedState = this.toOwned(child.isOwned());
        if (!child.wrapsObject(contents)) {
            throw new IllegalArgumentException("Wrong child object");
        }
        this.updateThisWrapper(key, child, contents);
        return updatedState;
    }

    /**
     * @param key
     * @param child
     */
    protected abstract <C extends Object> void updateThisWrapper(Object key, CowWrapper child, C contents);

}
