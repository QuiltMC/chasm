package org.quiltmc.chasm.lang.internal.render;

import org.quiltmc.chasm.lang.api.ast.Node;

public class Renderer {

    private final int indentation;
    private final char indentationChar;
    private final boolean prettyPrinting;
    private final boolean trailingNewline;
    private final boolean trailingCommas;

    private Renderer(
            int indentation,
            char indentationChar,
            boolean prettyPrinting,
            boolean trailingNewline,
            boolean trailingCommas
    ) {
        this.indentation = indentation;
        this.indentationChar = indentationChar;
        this.prettyPrinting = prettyPrinting;
        this.trailingNewline = trailingNewline;
        this.trailingCommas = trailingCommas;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String render(Node expression) {
        StringBuilder sb = new StringBuilder();
        expression.render(this, sb, 1);
        if (trailingNewline) {
            sb.append('\n');
        }
        return sb.toString();
    }

    public int getIndentation() {
        return indentation;
    }

    public char getIndentationChar() {
        return indentationChar;
    }

    public boolean hasPrettyPrinting() {
        return prettyPrinting;
    }

    public boolean hasTrailingNewline() {
        return trailingNewline;
    }

    public boolean hasTrailingCommas() {
        return trailingCommas;
    }

    public void indent(StringBuilder builder, int currentIndentationMultiplier) {
        if (!prettyPrinting) {
            return;
        }
        builder.append('\n');
        for (int i = 0; i < indentation * currentIndentationMultiplier; i++) {
            builder.append(indentationChar);
        }
    }

    public static class Builder {
        private int indentation;
        private char indentationChar;
        private boolean prettyPrinting;
        private boolean trailingNewline;
        private boolean trailingCommas;

        private Builder() {
            indentation = 4;
            indentationChar = ' ';
            prettyPrinting = false;
            trailingNewline = true;
            trailingCommas = true;
        }

        public Builder indentation(int value) {
            indentation = value;
            return this;
        }

        public Builder indentationChar(char value) {
            indentationChar = value;
            return this;
        }

        public Builder prettyPrinting(boolean value) {
            prettyPrinting = value;
            return this;
        }

        public Builder trailingNewline(boolean value) {
            trailingNewline = value;
            return this;
        }

        public Builder trailingCommas(boolean value) {
            trailingCommas = value;
            return this;
        }

        public Renderer build() {
            return new Renderer(
                    indentation,
                    indentationChar,
                    prettyPrinting,
                    trailingNewline,
                    trailingCommas
            );
        }
    }
}
