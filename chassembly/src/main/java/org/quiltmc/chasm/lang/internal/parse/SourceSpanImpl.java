package org.quiltmc.chasm.lang.internal.parse;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;

public class SourceSpanImpl implements SourceSpan {
    private final int lineStart;
    private final int columnStart;
    private final int lineEnd;
    private final int columnEnd;

    public SourceSpanImpl(int lineStart, int columnStart, int lineEnd, int columnEnd) {
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
    }

    public int getLineStart() {
        return lineStart;
    }

    public int getColumnStart() {
        return columnStart;
    }

    public int getLineEnd() {
        return lineEnd;
    }

    public int getColumnEnd() {
        return columnEnd;
    }

    public static SourceSpanImpl fromToken(Token t) {
        return new SourceSpanImpl(t.getBeginLine(), t.getBeginColumn(), t.getEndLine(), t.getEndColumn());
    }
    
    public SourceSpanImpl join(@NotNull SourceSpan other) {
        int lineStart;
        int columnStart;
        if (this.lineStart < other.getLineStart()) {
            lineStart = this.lineStart;
            columnStart = this.columnStart;
        } else if (other.getLineStart() < this.lineStart) {
            lineStart = other.getLineStart();
            columnStart = other.getColumnStart();
        } else {
            lineStart = this.lineStart;
            columnStart = Math.min(this.columnStart, other.getColumnStart());
        }

        int lineEnd;
        int columnEnd;
        if (this.lineEnd > other.getLineEnd()) {
            lineEnd = this.lineEnd;
            columnEnd = this.columnEnd;
        } else if (other.getLineEnd() > this.lineEnd) {
            lineEnd = other.getLineEnd();
            columnEnd = other.getColumnEnd();
        } else {
            lineEnd = this.lineEnd;
            columnEnd = Math.max(this.columnEnd, other.getColumnEnd());
        }

        return new SourceSpanImpl(lineStart, columnStart, lineEnd, columnEnd);
    }
}
