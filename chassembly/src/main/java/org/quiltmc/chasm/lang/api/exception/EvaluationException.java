package org.quiltmc.chasm.lang.api.exception;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;

/**
 * Thrown when chassembly evaluation fails.
 */
public class EvaluationException extends RuntimeException {
    /**
     * Creates an {@linkplain EvaluationException} with the given message.
     */
    public EvaluationException(String message) {
        super(message);
    }

    /**
     * Creates an {@linkplain EvaluationException} with the given message, thrown at the given source span.
     * @param message the message
     * @param span the source span, if null no source information will be included in the error message
     */
    public EvaluationException(String message, @Nullable SourceSpan span) {
        super(message + (span != null ? (" in " + span.asString()) : ""));
    }
}
