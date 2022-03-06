package org.quiltmc.chasm.lang.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;
import org.quiltmc.chasm.lang.antlr.ChasmBaseVisitor;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.ast.BinaryBooleanExpression;
import org.quiltmc.chasm.lang.ast.BinaryExpression;
import org.quiltmc.chasm.lang.ast.ConstantBooleanExpression;
import org.quiltmc.chasm.lang.ast.CallExpression;
import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.FilterExpression;
import org.quiltmc.chasm.lang.ast.FunctionExpression;
import org.quiltmc.chasm.lang.ast.IndexExpression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.ast.ListExpression;
import org.quiltmc.chasm.lang.ast.MapExpression;
import org.quiltmc.chasm.lang.ast.NoneExpression;
import org.quiltmc.chasm.lang.ast.ReferenceExpression;
import org.quiltmc.chasm.lang.ast.StringExpression;
import org.quiltmc.chasm.lang.ast.TernaryExpression;
import org.quiltmc.chasm.lang.ast.TypeExpression;
import org.quiltmc.chasm.lang.ast.UnaryExpression;

public class ChasmExpressionVisitor extends ChasmBaseVisitor<Expression> {
    @Override
    public Expression visitReferenceExpression(ChasmParser.ReferenceExpressionContext ctx) {
        return new ReferenceExpression(ctx.IDENTIFIER().getText());
    }

    @Override
    public IndexExpression visitMemberExpression(ChasmParser.MemberExpressionContext ctx) {
        return new IndexExpression(ctx.expression().accept(this), new StringExpression(ctx.IDENTIFIER().getText()));
    }

    @Override
    public Expression visitIndexExpression(ChasmParser.IndexExpressionContext ctx) {
        return new IndexExpression(ctx.expression(0).accept(this), ctx.expression(1).accept(this));
    }

    @Override
    public FilterExpression visitFilterExpression(ChasmParser.FilterExpressionContext ctx) {
        return new FilterExpression(ctx.expression(0).accept(this), ctx.expression(1).accept(this));
    }

    @Override
    public CallExpression visitCallExpression(ChasmParser.CallExpressionContext ctx) {
        return new CallExpression(ctx.expression(0).accept(this), ctx.expression(1).accept(this));
    }

    @Override
    public MapExpression visitMap(ChasmParser.MapContext ctx) {
        Map<String, Expression> entries = new LinkedHashMap<>();

        for (ChasmParser.MapEntryContext entry : ctx.mapEntry()) {
            entries.put(entry.IDENTIFIER().getText(), entry.expression().accept(this));
        }

        return new MapExpression(entries);
    }

    @Override
    public ListExpression visitList(ChasmParser.ListContext ctx) {
        List<Expression> entries = new ArrayList<>();

        for (ChasmParser.ExpressionContext entry : ctx.expression()) {
            entries.add(entry.accept(this));
        }

        return new ListExpression(entries);
    }

    @Override
    public StringExpression visitStringLiteral(ChasmParser.StringLiteralContext ctx) {
        String rawValue = ctx.STRING().getText();
        String value = rawValue.substring(1, rawValue.length() - 1);
        return new StringExpression(value);
    }

    @Override
    public Expression visitTypeLiteral(ChasmParser.TypeLiteralContext ctx) {
        String rawValue = ctx.TYPE().getText();
        Type value = Type.getType(rawValue.substring(2, rawValue.length() - 1));
        return new TypeExpression(value);
    }

    @Override
    public IntegerExpression visitIntegerLiteral(ChasmParser.IntegerLiteralContext ctx) {
        String rawValue = ctx.INTEGER().getText();
        int value = Integer.parseInt(rawValue);
        return new IntegerExpression(value);
    }

    @Override
    public ConstantBooleanExpression visitBooleanLiteral(ChasmParser.BooleanLiteralContext ctx) {
        String rawValue = ctx.BOOLEAN().getText();
        boolean value = Boolean.parseBoolean(rawValue);
        return new ConstantBooleanExpression(value);
    }

    @Override
    public NoneExpression visitNoneLiteral(ChasmParser.NoneLiteralContext ctx) {
        return Expression.none();
    }

    @Override
    public Expression visitGroupExpression(ChasmParser.GroupExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitBinaryExpression(ChasmParser.BinaryExpressionContext ctx) {
        return new BinaryExpression(
                ctx.expression(0).accept(this),
                BinaryExpression.Operation.of(ctx.op.getText()),
                ctx.expression(1).accept(this)
        );
    }

    @Override
    public Expression visitBinaryBooleanExpression(ChasmParser.BinaryBooleanExpressionContext ctx) {
        return new BinaryBooleanExpression(
                ctx.expression(0).accept(this),
                BinaryBooleanExpression.Operation.of(ctx.op.getText()),
                ctx.expression(1).accept(this)
        );
    }

    @Override
    public Expression visitUnaryExpression(ChasmParser.UnaryExpressionContext ctx) {
        return new UnaryExpression(
                UnaryExpression.Operation.of(ctx.op.getText()),
                ctx.expression().accept(this)
        );
    }

    @Override
    public Expression visitTernaryExpression(ChasmParser.TernaryExpressionContext ctx) {
        return new TernaryExpression(
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.expression(2).accept(this)
        );
    }

    @Override
    public FunctionExpression visitLambdaExpression(ChasmParser.LambdaExpressionContext ctx) {
        return new FunctionExpression(ctx.IDENTIFIER().getText(), ctx.expression().accept(this));
    }
}
