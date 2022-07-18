package org.quiltmc.chasm.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.ast.AbstractMapExpression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.ast.SimpleListExpression;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.FunctionExpression;
import org.quiltmc.chasm.lang.op.ListExpression;

public class Intrinsics {
    public static final Scope SCOPE = Scope.map(new Intrinsics().entries);
    private final HashMap<String, Expression> entries = new HashMap<>();

    private Intrinsics() {
        entries.put("len", len());
        entries.put("flatten", flatten());
        entries.put("map", map());
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

    private Expression flatten() {
        return new IntrinsicFunctionExpression(expression -> {
           if (expression instanceof ListExpression) {
               List<Expression> results = new ArrayList<>();
                for (Expression entry : (ListExpression) expression) {
                    if (entry instanceof ListExpression) {
                        for (Expression entry1 : (ListExpression) entry) {
                            results.add(entry1);
                        }
                    } else {
                        throw new RuntimeException("Function flatten can only be applied to lists of lists");
                    }
                }

                return new SimpleListExpression(expression.getParseTree(), results);
           } else {
               throw new RuntimeException("Function flatten can only be applied to lists of lists");
           }
        });
    }

    private Expression map() {
        return new IntrinsicFunctionExpression(expression -> {
            if (expression instanceof AbstractMapExpression) {
                AbstractMapExpression args = ((AbstractMapExpression) expression);
                Expression listArg = args.get("list");
                Expression functionArg = args.get("function");
                if (!(listArg instanceof ListExpression) || !(functionArg instanceof FunctionExpression)) {
                    throw new RuntimeException("Function map can only be applied to args: {list, function}");
                }

                ListExpression list = (ListExpression) listArg;
                FunctionExpression function = (FunctionExpression) functionArg;

                List<Expression> results = new ArrayList<>();
                for (Expression entry : list) {
                    results.add(function.call(entry));
                }

                return new SimpleListExpression(expression.getParseTree(), results);
            } else {
                throw new RuntimeException("Function map can only be applied to args: {list, function}");
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
