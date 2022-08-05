package org.quiltmc.chasm.lang.api.exception;

import org.quiltmc.chasm.lang.api.eval.Evaluator;

public class EvaluationException extends RuntimeException {
    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException withTraceback(Evaluator evaluator) {
        EvaluationException ex = new EvaluationException(this.getMessage() + "\n " + evaluator.renderTrace());
        ex.setStackTrace(this.getStackTrace());

        return ex;
    }
}
