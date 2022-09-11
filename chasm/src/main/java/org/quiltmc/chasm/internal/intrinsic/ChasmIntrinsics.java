package org.quiltmc.chasm.internal.intrinsic;

import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;

public final class ChasmIntrinsics {
    private ChasmIntrinsics() {
    }

    public static Evaluator makeEvaluator(Node node, Context context) {
        return Evaluator.builder(node)
            .addIntrinsic(new FileBytesIntrinsic(context))
            .addIntrinsic(new FileContentIntrinsic(context))
            .addIntrinsic(new IncludeIntrinsic(context))
            .build();
    }
}
