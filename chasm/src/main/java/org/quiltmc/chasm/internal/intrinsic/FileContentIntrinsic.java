package org.quiltmc.chasm.internal.intrinsic;

import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class FileContentIntrinsic extends IntrinsicFunction {
    private final Context context;

    public FileContentIntrinsic(Context context) {
        this.context = context;
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        String content = readString(arg, context);
        return content == null ? new NullNode() : new StringNode(content);
    }

    @Override
    public String getName() {
        return "file_content";
    }

    @Nullable
    static String readString(Node arg, Context context) {
        if (!(arg instanceof StringNode)) {
            throw new EvaluationException(
                "Built-in function \"file_content\" can only be applied to strings but found " + arg);
        }
        String path = ((StringNode) arg).getValue();
        byte[] bytes = context.readFile(path);
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new EvaluationException("File \"" + path + "\" is not utf8 encoded");
        }
    }
}
