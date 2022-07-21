package org.quiltmc.chasm.lang.internal.render;

import org.quiltmc.chasm.lang.api.ast.Node;

public class Renderer {
    public static String render(Node expression, RendererConfig config) {
        StringBuilder sb = new StringBuilder();
        expression.render(config, sb, 1);
        if (config.insertEndingNewline()) {
            sb.append('\n');
        }
        return sb.toString();
    }
}
