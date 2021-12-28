/**
 *
 */
package org.quiltmc.chasm.internal.cow;

/**
 * Wraps an object in a lockable COW wrapper that only copies to unlock.
 *
 * @param <T> The object to wrap.
 * @param <W> A superclass of {@code this} that extends abstract cow wrapper.
 */
public abstract class AbstractCowWrapper<T extends Copyable, W extends AbstractCowWrapper<T, W>>
        implements UpdatableCowWrapper {

    /**
     * The reference to the wrapped object.
     *
     * Read-only accesses may happen in any {@code CowState}, but write accesses
     * must only happen in a shared state.
     */
    protected T object;
    private CowState state;

    /**
     * Shallow copies this wrapper.
     *
     * @param other The wrapper to copy.
     */
    protected AbstractCowWrapper(AbstractCowWrapper<T, W> other) {
        this(other.object, other.isOwned());
    }

    /**
     * Constructs this wrapper with the given state.
     *
     * @param object The object to wrap.
     * @param owned Whether this object requires copying before it is writable.
     */
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

    /**
     * Returns {@code this} cast to {@link AbstractCowWrapper}.W.
     *
     * @return {@code this} as a W.
     */
    protected abstract W castThis();

    @Override
    public abstract W deepCopy();

    @Override
    public abstract W shallowCopy();

    /**
     * Returns a shared shallow copied {@code this}.
     *
     * @return A shallow copy of this as a shared wrapper.
     */
    public final W getShared() {
        if (this.state == CowState.SHARED) {
            return castThis();
        }
        W newWrapper = this.shallowCopy();
        newWrapper.toShared();
        return newWrapper;
    }

    /**
     * Returns an owned shallow copy of {@code this}.
     *
     * <p>Will shallow copy this wrapper's contained object if required.
     *
     * @return An owned shallow copy of this wrapper.
     */
    public final W getOwned() {
        if (this.state == CowState.OWNED) {
            return castThis();
        }
        W newWrapper = this.shallowCopy();
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

    /**
     * Shallow copies the contained object, and switches to owned state.
     */
    @SuppressWarnings("unchecked")
    protected void copyToOwn() {
        this.object = (T) object.shallowCopy();
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
    public final boolean updateParentWrapper(Object key, UpdatableCowWrapper child, Object contents) {
        final boolean oldState = this.isOwned();
        if (!child.wrapsObject(contents) || !child.checkParentLink(this) || !child.checkKey(key)) {
            throw new IllegalArgumentException("Wrong child object");
        }
        this.updateThisWrapper(key, child, contents);
        return this.isOwned() != oldState;
    }

    /**
     * @param key
     * @param child
     * @param contents
     */
    protected abstract void updateThisWrapper(Object key, UpdatableCowWrapper child, Object contents);
}
