package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public abstract class IntrinsicFunction extends FunctionNode {
    abstract String getName();

    @Override
    public final void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier, OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.ARGUMENT_PRIMARY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }
        builder.append(getName());
        if (needsBrackets) {
            builder.append(')');
        }
    }
}
