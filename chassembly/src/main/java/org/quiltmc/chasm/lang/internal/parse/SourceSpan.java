package org.quiltmc.chasm.lang.internal.parse;

public class SourceSpan {
    private final int lineStart;
    private final int columnStart;
    private final int lineEnd;
    private final int columnEnd;

    public SourceSpan(int lineStart, int columnStart, int lineEnd, int columnEnd) {
        this.lineStart = lineStart;
        this.lineEnd = lineEnd;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
    }

    public static SourceSpan fromToken(Token t) {
        return new SourceSpan(t.getBeginLine(), t.getBeginColumn(), t.getEndLine(), t.getEndColumn());
    }
    
    public SourceSpan join(SourceSpan other) {
        int lineStart;
        int columnStart;
        if (this.lineStart < other.lineStart) {
            lineStart = this.lineStart;
            columnStart = this.columnStart;
        } else if (other.lineStart < this.lineStart) {
            lineStart = other.lineStart;
            columnStart = other.columnStart;
        } else {
            lineStart = this.lineStart;
            columnStart = Math.min(this.columnStart, other.columnStart);
        }

        int lineEnd;
        int columnEnd;
        if (this.lineEnd > other.lineEnd) {
            lineEnd = this.lineEnd;
            columnEnd = this.columnEnd;
        } else if (other.lineEnd > this.lineEnd) {
            lineEnd = other.lineEnd;
            columnEnd = other.columnEnd;
        } else {
            lineEnd = this.lineEnd;
            columnEnd = Math.max(this.columnEnd, other.columnEnd);
        }

        return new SourceSpan(lineStart, columnStart, lineEnd, columnEnd);
    }
}
