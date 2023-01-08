package org.quiltmc.chasm.lang.api.exception;

import org.quiltmc.chasm.lang.api.eval.SourceSpan;

public class EvaluationException extends RuntimeException {
    public EvaluationException(String message, SourceSpan span) {
        super(message + "in " + span.asString());
    }
}
