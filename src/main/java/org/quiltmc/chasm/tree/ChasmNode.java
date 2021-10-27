package org.quiltmc.chasm.tree;

public interface ChasmNode {
    ChasmNode evaluate(EvaluationContext context);
}
