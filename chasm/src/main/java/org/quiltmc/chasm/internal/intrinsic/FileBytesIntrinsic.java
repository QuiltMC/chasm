package org.quiltmc.chasm.internal.intrinsic;

import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.parse.SourceSpanImpl;

public class FileBytesIntrinsic extends IntrinsicFunction {
    private final Context context;

    public FileBytesIntrinsic(Context context) {
        this.context = context;
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof StringNode)) {
            throw new EvaluationException(
                "Built-in function \"file_bytes\" can only be applied to strings but found " + arg,
                    arg.getMetadata().get(SourceSpanImpl.class));
        }
        byte[] bytes = context.readFile(((StringNode) arg).getValue());
        if (bytes == null) {
            return Ast.nullNode();
        }
        ListNode result = Ast.emptyList();
        for (byte b : bytes) {
            result.add(Ast.literal(b & 0xff));
        }
        return result;
    }

    @Override
    public String getName() {
        return "file_bytes";
    }
}
