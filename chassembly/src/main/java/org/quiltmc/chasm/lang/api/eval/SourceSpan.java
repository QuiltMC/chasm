package org.quiltmc.chasm.lang.api.eval;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.chasm.lang.internal.parse.SourceSpanImpl;

public interface SourceSpan {
    @Contract(pure = true)
    int getLineStart();

    @Contract(pure = true)
    int getColumnStart();

    @Contract(pure = true)
    int getLineEnd();

    @Contract(pure = true)
    int getColumnEnd();

    @Contract(pure = true, value = "_->new")
    SourceSpan join(@NotNull SourceSpan other);

    static SourceSpan from(int lineStart, int columnStart, int lineEnd, int columnEnd) {
        return new SourceSpanImpl(lineStart, columnStart, lineEnd, columnEnd);
    }

    static SourceSpan from(int line, int column) {
        return new SourceSpanImpl(line, column, line, column);
    }

    static SourceSpan endOf(SourceSpan span) {
        return from(span.getLineEnd(), span.getColumnEnd());
    }

    static SourceSpan startOf(SourceSpan span) {
        return from(span.getLineStart(), span.getColumnStart());
    }

    default String asString() {
        StringBuilder builder = new StringBuilder();
        if (getLineStart() == getLineEnd()) {
            builder.append("line " + getLineStart() + ", column " + getColumnStart());
            if (!(getColumnStart() == getColumnEnd())) {
                builder.append("-" + getColumnEnd());
            }
        } else {
            builder.append("line " + getLineStart() + ", column " + getColumnStart());
            builder.append(" to line " + getLineEnd() + ", column " + getColumnEnd());
        }
        builder.append(
                " (" + getLineStart() + ":" + getColumnStart() + "-" + getLineEnd() + ":" + getColumnEnd() + ")"
        );
        return builder.toString();
    }
}
