package org.quiltmc.chasm.lang.api.exception;

import org.quiltmc.chasm.lang.api.eval.SourceSpan;

/**
 * Thrown when chassembly evaluation fails.
 */
public class EvaluationException extends RuntimeException {
    /**
     * Creates an {@linkplain EvaluationException} with the given message.
     */
    public EvaluationException(String message, SourceSpan span) {
        super(message + "in " + span.asString());
    }
}
