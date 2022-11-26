package org.quiltmc.chasm.lang.api.exception;

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
}
