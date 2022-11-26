package org.quiltmc.chasm.lang.api.eval;

import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A function that is implemented in Java rather than in chassembly. Some built-in intrinsic functions may also be
 * treated specially by the evaluator as well.
 */
public abstract class IntrinsicFunction extends FunctionNode {
    /**
     * Returns the name of the intrinsic function.
     */
    public abstract String getName();

    @Override
    public final void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(getName());
    }
}
