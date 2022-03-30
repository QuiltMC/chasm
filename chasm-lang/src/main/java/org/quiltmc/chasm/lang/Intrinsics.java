package org.quiltmc.chasm.lang;

import java.util.HashMap;
import java.util.function.Function;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.FunctionExpression;
import org.quiltmc.chasm.lang.op.ListExpression;

public class Intrinsics {
    public static final Scope SCOPE = Scope.map(new Intrinsics().entries);
    private final HashMap<String, Expression> entries = new HashMap<>();

    private Intrinsics() {
        entries.put("len", len());
    }

    private Expression len() {
        return new IntrinsicFunctionExpression(expression -> {
            if (expression instanceof ListExpression) {
                return new IntegerExpression(null, ((ListExpression) expression).getLength());
            } else {
                throw new RuntimeException("Function len can only be applied to lists");
            }
        });
    }

    static class IntrinsicFunctionExpression implements FunctionExpression {
        private final Function<Expression, Expression> func;

        public IntrinsicFunctionExpression(Function<Expression, Expression> func) {
            this.func = func;
        }

        @Override
        public ParseTree getParseTree() {
            return null;
        }

        @Override
        public Expression resolve(ScopeStack scope) {
            return this;
        }

        @Override
        public Expression reduce(Cache cache) {
            return this;
        }

        @Override
        public Expression call(Expression argument) {
            return func.apply(argument);
        }
    }
}
