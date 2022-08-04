package org.quiltmc.chasm.lang.api.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.ParseException;
import org.quiltmc.chasm.lang.internal.parse.Parser;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * The base class used to represent Nodes in the abstract syntax tree.
 * This class can be extended by an execution environment if it requires special behaviour for custom nodes.
 */
public abstract class Node {
    @ApiStatus.OverrideOnly
    public abstract void resolve(Resolver resolver);

    public abstract Node evaluate(Evaluator evaluator);


    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        this.render(renderer, builder, currentIndentationMultiplier, OperatorPriority.ANY);
    }

    public abstract void render(
            Renderer renderer, StringBuilder builder, int indentation, OperatorPriority minPriority);

    /**
     * Parse a given file into a {@link Node}.
     *
     * @param path Path to a valid source file containing a single node.
     * @return The node parsed from the given source file.
     * @throws IOException If there is an error reading the file.
     * @throws ParseException If the source contains syntax errors.
     */
    public static Node parse(Path path) throws IOException {
        Parser parser = new Parser(path);
        return parser.file();
    }

    /**
     * Parse a given {@link String} into a {@link Node}.
     *
     * @param string A string containing a single node.
     * @return The node parsed from the given source file.
     * @throws ParseException If the source contains syntax errors.
     */
    public static Node parse(String string) {
        Parser parser = new Parser(string);
        return parser.file();
    }

    public void write(Path path) throws IOException {
        Renderer renderer = Renderer.builder().build();
        StringBuilder sb = new StringBuilder();
        render(renderer, sb, 1);
        Files.write(path, sb.toString().getBytes()); // what about utf16 support?
    }
}
