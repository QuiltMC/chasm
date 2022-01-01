/**
 *
 */
package org.quiltmc.chasm.internal.cow;

import org.quiltmc.chasm.api.util.CowWrapper;

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

    /**
     * Retrieves the cached {@link CowWrapper} corresponding to the passed key, or {@code null}.
     *
     * @param key The key of the cached child wrapper to fetch.
     *
     * @return The cached cow wrapper with the given key.
     *
     * @exception ClassCastException If the given key is not of the required type.
     */
    protected abstract CowWrapper getCachedCowWrapper(Object key);

    /**
     * Sets the cached {@link CowWrapper} corresponding to the passed key.
     *
     * @param key The key to insert the cached child wrapper under.
     * @param wrapper The old cached cow wrapper, or {@code null} if no wrapper was cached.
     *
     * @return The old cached {@code CowWrapper} with the given key.
     *
     * @exception ClassCastException If the given key is not the correct type, or the wrapper is not the correct type.
     */
    protected abstract CowWrapper setCachedCowWrapper(Object key, CowWrapper wrapper);

    /**
     * Gets the contained Object's child corresponding to the passed key.
     *
     * @param key The key to retrieve the contained Object's child.
     *
     * @return The child of the contained Object with the given key.
     *
     * @exception ClassCastException If the given key is not of the expected type.
     */
    protected abstract Object getChildObject(Object key);

    /**
     * Sets the contained Object's child corresponding to the passed key.
     *
     * @param key The key specifies which child to set.
     * @param value The old contained Object's child with the given key.
     *
     * @return The old child of the contained Object with the passed key.
     */
    protected abstract Object setChildObject(Object key, Object value);

    /**
     * Updates this wrapper as the parent of the passed wrapper.
     *
     * <p>If the passed wrapper does not match the current child wrapper of {@code this}, clears the passed child
     * wrapper's parent link to this wrapper and does not change the state of this wrapper.
     *
     * <p>If the passed wrapper is in the same {@code CowState} as this wrapper but has a different contained object,
     * updates this wrapper's contained object's child to match the passed child's contained object.
     * If the passed wrapper is in the same {@code CowState} and contains the same object as this wrapper's contained
     * object's child object with the given key, does not change the state of this wrapper.
     *
     * <p>If the passed child object is in a different {@code CowState} from this wrapper, sets the ownership state of
     * this wrapper to match the child wrapper's.
     *
     * @param key
     * @param child
     * @param contents
     *
     * @return Whether the state of this {@link CowWrapper} changed after this call.
     *
     * @exception IllegalArgumentException if the passed child does not contain the passed contents or the passed
     *                child's parent wrapper is not {@code this}.
     * @exception ClassCastException if the passed key, child, or contents are not the type expected by the implementing
     *                subclass.
     */
    @Override
    public final boolean updateWrapper(Object key, UpdatableCowWrapper child, Object contents) {
        if (!child.wrapsObject(contents) || !child.checkParentLink(this) || !child.checkKey(key)) {
            throw new IllegalArgumentException("Invalid child object");
        }
        CowWrapper childWrapper = getCachedCowWrapper(key);
        if (childWrapper != child) {
            // Leftover parent link, so sever it
            child.unlinkParentWrapper();
            return false;
        }
        Object childObject = getChildObject(key);
        if (this.isOwned() == child.isOwned()) {
            if (contents != childObject) {
                this.setChildObject(key, contents);
                return true;
            } else {
                // The child wrapper matches this cached wrapper perfectly
                return false;
            }
        } else { // this.isOwned != child.isOwned()
            if (child.isOwned()) {
                this.toOwned();
                this.setChildObject(key, contents);
                this.setCachedCowWrapper(key, child);
            } else {
                this.toShared();
            }
            return true;
        }
    }
}
