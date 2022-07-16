package org.quiltmc.chasm.lang.api.ast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.quiltmc.chasm.lang.internal.parse.Parser;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public abstract class Expression {
    public abstract Expression copy();

    public static Expression parse(String expression) {
        Parser parser = new Parser(expression);
        return parser.file();
    }

    public String render() {
        Renderer renderer = new Renderer();
        return renderer.render(this);
    }

    public static Expression read(Path path) throws IOException {
        Parser parser = new Parser(path);
        return parser.file();
    }

    public void write(Path path) throws IOException {
        Files.write(path, render().getBytes());
    }
}
