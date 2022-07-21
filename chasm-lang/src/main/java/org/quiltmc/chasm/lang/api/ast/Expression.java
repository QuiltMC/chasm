package org.quiltmc.chasm.lang.api.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.quiltmc.chasm.lang.internal.parse.Parser;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public abstract class Expression {
    protected static void indent(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        if (!config.prettyPrinting()) {
            return;
        }
        builder.append('\n');
        for (int i = 0; i < config.indentSize() * currentIndentationMultiplier; i++) {
            builder.append(config.indentationChar());
        }
    }

    public abstract Expression copy();

    public static Expression parse(String expression) {
        Parser parser = new Parser(expression);
        return parser.file();
    }

    public abstract void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier);

    public static Expression read(Path path) throws IOException {
        Parser parser = new Parser(path);
        return parser.file();
    }

    public void write(Path path, RendererConfig config) throws IOException {
        StringBuilder sb = new StringBuilder();
        render(config, sb, 1);
        Files.write(path, sb.toString().getBytes()); // what about utf16 support?
    }
}
