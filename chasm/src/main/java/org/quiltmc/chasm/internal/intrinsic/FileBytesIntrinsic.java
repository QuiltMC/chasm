package org.quiltmc.chasm.internal.intrinsic;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class FileBytesIntrinsic extends IntrinsicFunction {
    private final Context context;

    public FileBytesIntrinsic(Context context) {
        this.context = context;
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof StringNode)) {
            throw new EvaluationException(
                "Built-in function \"file_bytes\" can only be applied to strings but found " + arg);
        }
        byte[] bytes = context.readFile(((StringNode) arg).getValue());
        return bytes == null ? new NullNode() : new ListNode(IntStream.range(0, bytes.length)
                .mapToObj(index -> new IntegerNode(bytes[index] & 0xff)).collect(Collectors.toList()));
    }

    @Override
    public String getName() {
        return "file_bytes";
    }
}
