package org.quiltmc.chasm.tree;

public class ChasmValue<T> implements ChasmNode {
    private final T value;

    public ChasmValue(T value) {
        this.value = value;
    }

    @Override
    public ChasmValue<T> evaluate(EvaluationContext context) {
        return this;
    }

    public T getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "none";
        }

        return value.toString();
    }
}
