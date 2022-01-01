/**
 *
 */
package org.quiltmc.chasm.internal.cow;

public abstract class AbstractChildCowWrapper<T extends Copyable, W extends AbstractChildCowWrapper<T, W, P>, P extends UpdatableCowWrapper>
        extends AbstractCowWrapper<T, W> {
    private P parent;
    private Object key;

    public enum SentinelKeys {
        METADATA;
    }

    /**
     * @param other
     */
    protected AbstractChildCowWrapper(AbstractChildCowWrapper<T, W, P> other) {
        super(other);
        this.parent = other.parent;
        this.key = other.key;
    }

    protected <K extends Object> AbstractChildCowWrapper(P parent, K key, T object, boolean owned) {
        super(object, owned);
        this.parent = parent;
        this.key = key;
    }

    @Override
    protected final void copyToOwn() {
        super.copyToOwn();
        if (this.parent != null) {
            this.parent.updateWrapper(key, this, this.object);
        }
    }

    @Override
    public boolean unlinkParentWrapper() {
        if (this.parent == null) {
            return false;
        } else {
            this.parent = null;
            return true;
        }
    }

    @Override
    public boolean checkParentLink(Object o) {
        return this.parent == o;
    }

    @Override
    public boolean checkKey(Object objKey) {
        return this.key == objKey;
    }
}
