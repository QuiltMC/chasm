package org.quiltmc.chasm.lang.internal.render;

public final class RendererConfigBuilder {
    private final int indentationSize;
    private final char indentationChar;
    private boolean prettyPrinting;
    private boolean insertEndingNewline;
    private boolean trailingCommas;

    private RendererConfigBuilder(int indentationSize, char indentationChar) {
        this.indentationSize = indentationSize;
        this.indentationChar = indentationChar;
        prettyPrinting = false;
        insertEndingNewline = false;
        trailingCommas = false;
    }

    public static RendererConfigBuilder create(int indentationSize, char indentationChar) {
        return new RendererConfigBuilder(indentationSize, indentationChar);
    }

    public RendererConfigBuilder prettyPrinting() {
        prettyPrinting = true;
        return this;
    }

    public RendererConfigBuilder insertEndingNewline() {
        insertEndingNewline = true;
        return this;
    }

    public RendererConfigBuilder trailingCommas() {
        trailingCommas = true;
        return this;
    }

    public RendererConfig build() {
        return new RendererConfig(indentationSize, indentationChar, prettyPrinting, insertEndingNewline, trailingCommas);
    }
}
