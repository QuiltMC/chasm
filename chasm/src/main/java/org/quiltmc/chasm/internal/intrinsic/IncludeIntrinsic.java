package org.quiltmc.chasm.internal.intrinsic;

import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.api.exception.ParseException;

public class IncludeIntrinsic extends IntrinsicFunction {
    private final Context context;

    public IncludeIntrinsic(Context context) {
        this.context = context;
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        String content = FileContentIntrinsic.readString(arg, context);
        if (content == null) {
            throw new EvaluationException(
                    "Could not read file " + arg,
                    arg.getMetadata().get(SourceSpan.class)
            );
        }
        Node result;
        try {
            result = Node.parse(content);
        } catch (ParseException e) {
            throw new EvaluationException(
                    "Failed to parse file " + arg + ": " + e.getMessage(),
                    arg.getMetadata().get(SourceSpan.class)
            );
        }
        result.resolve(evaluator.getResolver());
        return result.evaluate(evaluator);
    }

    @Override
    public String getName() {
        return "include";
    }
}
