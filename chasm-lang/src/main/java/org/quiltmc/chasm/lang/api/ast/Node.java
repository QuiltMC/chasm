package org.quiltmc.chasm.lang.api.ast;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.ParseException;
import org.quiltmc.chasm.lang.internal.parse.Parser;
import org.quiltmc.chasm.lang.internal.render.Renderer;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

/**
 * The base class used to represent Nodes in the abstract syntax tree.
 * This class can be extended by an execution environment if it requires special behaviour for custom nodes.
 */
public abstract class Node {
    /**
     * Creates a deep copy of this {@link Node}.
     * An immutable node may return itself.
     *
     * @return A deep copy of this node.
     *
     */
    public abstract Node copy();

    @ApiStatus.OverrideOnly
    public abstract void resolve(Resolver resolver);

    public abstract Node evaluate(Evaluator evaluator);

    public abstract void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier);


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

    public void write(Path path, RendererConfig config) throws IOException {
        StringBuilder sb = new StringBuilder();
        render(config, sb, 1);
        Files.write(path, sb.toString().getBytes()); // what about utf16 support?
    }

    /**
     * Convert this {@link Node} into its string representation.
     *
     * @return A string that can be parsed using {@link #parse(String)}.
     */
    public final String compose() {
        return Renderer.render(this, new RendererConfig(4, ' ', true, true, true));
    }

    public final void compose(Path path) throws IOException {
        Files.write(path, compose().getBytes());
    }

    protected static void indent(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        if (!config.prettyPrinting()) {
            return;
        }
        builder.append('\n');
        for (int i = 0; i < config.indentSize() * currentIndentationMultiplier; i++) {
            builder.append(config.indentationChar());

        }
    }
}
