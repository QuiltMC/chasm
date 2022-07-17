package org.quiltmc.chasm.lang.internal.render;

public final class RendererConfig {
    private final int indentationSize;
    private final char indentationChar;
    private final boolean prettyPrinting;
    private final boolean insertEndingNewline;
    private final boolean trailingCommas;

    public RendererConfig(int indentationSize, char indentationChar, boolean prettyPrinting, boolean insertEndingNewline, boolean trailingCommas) {
        this.indentationSize = indentationSize;
        this.indentationChar = indentationChar;
        this.prettyPrinting = prettyPrinting;
        this.insertEndingNewline = insertEndingNewline;
        this.trailingCommas = trailingCommas;
    }

    public int indentSize() {
        return indentationSize;
    }

    public char indentationChar() {
        return indentationChar;
    }

    public boolean prettyPrinting() {
        return prettyPrinting;
    }

    public boolean insertEndingNewline() {
        return insertEndingNewline;
    }

    public boolean trailingCommas() {
        return trailingCommas;
    }
}
