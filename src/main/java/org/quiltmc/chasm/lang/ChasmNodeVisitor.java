package org.quiltmc.chasm.lang;

import org.quiltmc.chasm.tree.*;

public class ChasmNodeVisitor extends ChasmBaseVisitor<ChasmNode> {
    // ----------------------------------------------------
    //                     Expressions
    // ----------------------------------------------------
    @Override
    public ChasmNode visitFunctionCall(ChasmParser.FunctionCallContext ctx) {
        ChasmNode function = visit(ctx.expression(0));
        ChasmNode argument = visit(ctx.expression(1));

        return context -> {
            ChasmNode evalFunction = function.evaluate(context);

            if (evalFunction instanceof ChasmFunction func) {
                return func.getBody().evaluate(context.with(func.getParameterName(), argument.evaluate(context)));
            }
            else {
                throw new RuntimeException("Primary expression of function call must be a function.");
            }
        };
    }

    @Override
    public ChasmNode visitAddSubtract(ChasmParser.AddSubtractContext ctx) {
        ChasmNode lhs = visit(ctx.expression(0));
        ChasmNode rhs = visit(ctx.expression(1));
        String operator = ctx.getChild(1).getText();

        return context -> {
            if (lhs.evaluate(context) instanceof ChasmValue lValue && lValue.getValue() instanceof Integer lInt
                    && rhs.evaluate(context) instanceof ChasmValue rValue && rValue.getValue() instanceof Integer rInt) {
                return switch (operator) {
                    case "+" -> new ChasmValue<>(lInt + rInt);
                    case "-" -> new ChasmValue<>(lInt - rInt);
                    default -> throw new RuntimeException("Unimplemented operator " + operator);
                };
            }
            else {
                throw new RuntimeException("Math operation can only be applied to integers.");
            }
        };
    }

    @Override
    public ChasmNode visitMultiplyDivide(ChasmParser.MultiplyDivideContext ctx) {
        ChasmNode lhs = visit(ctx.expression(0));
        ChasmNode rhs = visit(ctx.expression(1));
        String operator = ctx.getChild(1).getText();

        return context -> {
            if (lhs.evaluate(context) instanceof ChasmValue lValue && lValue.getValue() instanceof Integer lInt
                    && rhs.evaluate(context) instanceof ChasmValue rValue && rValue.getValue() instanceof Integer rInt) {
                return switch (operator) {
                    case "*" -> new ChasmValue<>(lInt * rInt);
                    case "/" -> new ChasmValue<>(lInt / rInt);
                    default -> throw new RuntimeException("Unimplemented operator " + operator);
                };
            }
            else {
                throw new RuntimeException("Math operation can only be applied to integers.");
            }
        };
    }

    @Override
    public ChasmNode visitCompare(ChasmParser.CompareContext ctx) {
        ChasmNode lhs = visit(ctx.expression(0));
        ChasmNode rhs = visit(ctx.expression(1));
        String operator = ctx.getChild(1).getText();

        return context -> {
            if (lhs.evaluate(context) instanceof ChasmValue lValue && lValue.getValue() instanceof Integer lInt
                    && rhs.evaluate(context) instanceof ChasmValue rValue && rValue.getValue() instanceof Integer rInt) {
                return switch (operator) {
                    case "<" -> new ChasmValue<>(lInt < rInt);
                    case "=" -> new ChasmValue<>(lInt.equals(rInt));
                    case ">" -> new ChasmValue<>(lInt > rInt);
                    default -> throw new RuntimeException("Unimplemented operator " + operator);
                };
            }
            else {
                throw new RuntimeException("Math operation can only be applied to integers.");
            }
        };
    }

    @Override
    public ChasmNode visitTernary(ChasmParser.TernaryContext ctx) {
        ChasmNode condition = visit(ctx.expression(0));
        ChasmNode trueExpression = visit(ctx.expression(1));
        ChasmNode falseExpression = visit(ctx.expression(2));

        return context -> {
            ChasmNode evalCondition = condition.evaluate(context);
            if (evalCondition instanceof ChasmValue value && value.getValue() instanceof Boolean bool) {
                return bool ? trueExpression.evaluate(context) : falseExpression.evaluate(context);
            }
            else {
                throw new RuntimeException("Primary expression in ternary must be a boolean.");
            }
        };
    }

    @Override
    public ChasmNode visitMemberAccess(ChasmParser.MemberAccessContext ctx) {
        ChasmNode node = visit(ctx.expression());
        String member = ctx.IDENTIFIER().getText();

        return context -> {
            ChasmNode evaluated = node.evaluate(context);
            if (evaluated instanceof ChasmMap map) {
                return map.get(member);
            }
            else {
                throw new RuntimeException("Member access can only be performed for maps");
            }
        };
    }

    @Override
    public ChasmNode visitFilterOperation(ChasmParser.FilterOperationContext ctx) {
        ChasmNode lhs = visit(ctx.expression(0));
        ChasmNode filter = visit(ctx.expression(1));

        return context -> {
            if (lhs.evaluate(context) instanceof ChasmList list) {
                if (filter.evaluate(context) instanceof ChasmFunction filterFunc) {
                    ChasmList resultList = new ChasmList();
                    for (var entry : list) {
                        ChasmNode result = filterFunc.getBody().evaluate(context.with(filterFunc.getParameterName(), entry.evaluate(context)));
                        if (result instanceof ChasmValue rValue && rValue.getValue() instanceof Boolean rBool) {
                            if (rBool) {
                                resultList.add(entry);
                            }
                        }
                        else {
                            throw new RuntimeException("Result of filter function must be a boolean.");
                        }
                    }
                    return resultList;
                }
                else {
                    throw new RuntimeException("Filter must be a function");
                }
            }
            else {
                throw new RuntimeException("Filter operation can only be applied to lists");
            }
        };
    }

    @Override
    public ChasmNode visitLambdaDefinition(ChasmParser.LambdaDefinitionContext ctx) {
        String parameterName = ctx.IDENTIFIER().getText();
        ChasmNode body = visit(ctx.expression());

        return new ChasmFunction(parameterName, body);
    }

    @Override
    public ChasmNode visitReference(ChasmParser.ReferenceContext ctx) {
        String identifier = ctx.IDENTIFIER().getText();

        return context -> context.resolveReference(identifier);
    }

    // ----------------------------------------------------
    //                      Literals
    // ----------------------------------------------------
    @Override
    public ChasmNode visitBooleanLiteral(ChasmParser.BooleanLiteralContext ctx) {
        return new ChasmValue<>(Boolean.parseBoolean(ctx.BOOLEAN().getText()));
    }

    @Override
    public ChasmNode visitStringLiteral(ChasmParser.StringLiteralContext ctx) {
        String raw = ctx.STRING().getText();
        return new ChasmValue<>(raw.substring(1, raw.length() - 1));
    }

    @Override
    public ChasmNode visitIntegerLiteral(ChasmParser.IntegerLiteralContext ctx) {
        return new ChasmValue<>(Integer.parseInt(ctx.INTEGER().getText()));
    }

    @Override
    public ChasmNode visitNoneLiteral(ChasmParser.NoneLiteralContext ctx) {
        return new ChasmValue<>(null);
    }

    // ----------------------------------------------------
    //                        Map
    // ----------------------------------------------------
    @Override
    public ChasmMap visitMap(ChasmParser.MapContext ctx) {
        ChasmMap map = new ChasmMap();
        for (var entry : ctx.mapEntry()) {
            map.put(entry.IDENTIFIER().getText(), visit(entry.expression()));
        }
        return map;
    }

    // ----------------------------------------------------
    //                        List
    // ----------------------------------------------------
    @Override
    public ChasmList visitList(ChasmParser.ListContext ctx) {
        ChasmList list = new ChasmList();
        for (var entry : ctx.expression()) {
            list.add(visit(entry));
        }
        return list;
    }
}
