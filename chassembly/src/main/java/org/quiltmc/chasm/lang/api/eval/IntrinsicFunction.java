package org.quiltmc.chasm.lang.api.eval;

import org.quiltmc.chasm.lang.internal.render.Renderer;

public abstract class IntrinsicFunction extends FunctionNode {
    public abstract String getName();

    @Override
    public final void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(getName());
    }
}
