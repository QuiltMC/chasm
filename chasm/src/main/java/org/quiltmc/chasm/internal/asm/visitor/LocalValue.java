package org.quiltmc.chasm.internal.asm.visitor;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.analysis.Value;

import java.util.Arrays;
import java.util.Objects;

public final class LocalValue implements Value {
    private final @Nullable Type type;
    private final int @Nullable[] sourceStores;

    public LocalValue(@Nullable Type type) {
        this.type = type;
        this.sourceStores = null;
    }

    public LocalValue(@Nullable Type type, InsnList instructions, AbstractInsnNode sourceStore) {
        this.type = type;
        this.sourceStores = new int[]{instructions.indexOf(sourceStore)};
    }

    public LocalValue(@Nullable Type type, int @Nullable[] sourceStores) {
        this.type = type;
        this.sourceStores = sourceStores;
    }

    public int @Nullable[] getSourceStores() {
        return sourceStores;
    }

    @Nullable
    public Type getType() {
        return type;
    }

    @Override
    public int getSize() {
        return type == null ? 1 : type.getSize();
    }

    @Override
    public int hashCode() {
        return (31 * Arrays.hashCode(sourceStores)) + Objects.hashCode(type);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof LocalValue)) return false;
        LocalValue that = (LocalValue) other;
        return Arrays.equals(this.sourceStores, that.sourceStores) && Objects.equals(this.type, that.type);
    }

    @Override
    public String toString() {
        return Arrays.toString(sourceStores) + " -> " + type;
    }

    public static int @Nullable[] mergeSourceStores(int @Nullable[] a, int @Nullable[] b) {
        if (a == null) {
            return b;
        } else if (b == null) {
            return a;
        } else {
            int[] ret = new int[a.length + b.length];
            int aIndex = 0;
            int bIndex = 0;
            int retIndex = 0;
            while (aIndex < a.length || bIndex < b.length) {
                if (aIndex < a.length && (bIndex >= b.length || a[aIndex] <= b[bIndex])) {
                    if (bIndex < b.length && a[aIndex] == b[bIndex]) {
                        // prevent duplicates
                        bIndex++;
                    }
                    ret[retIndex++] = a[aIndex++];
                } else {
                    ret[retIndex++] = b[bIndex++];
                }
            }
            if (retIndex < ret.length) {
                return Arrays.copyOf(ret, retIndex);
            }
            return ret;
        }
    }
}
