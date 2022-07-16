package org.quiltmc.chasm.lang.api.eval;

import java.util.Collections;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.Expression;
import org.quiltmc.chasm.lang.internal.eval.EvaluatorImpl;

public interface Evaluator {
    Expression evaluate(Expression expression);

    Expression reduce(Expression expression);

    static Evaluator create() {
        return new EvaluatorImpl(Collections.emptyMap());
    }

    static Evaluator create(Map<String, Expression> globals) {
        return new EvaluatorImpl(globals);
    }
}
