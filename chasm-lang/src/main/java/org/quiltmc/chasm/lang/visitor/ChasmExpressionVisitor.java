package org.quiltmc.chasm.lang.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.lang.antlr.ChasmBaseVisitor;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.ast.BinaryBooleanExpression;
import org.quiltmc.chasm.lang.ast.BinaryExpression;
import org.quiltmc.chasm.lang.ast.CallExpression;
import org.quiltmc.chasm.lang.ast.ConstantBooleanExpression;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.ast.LambdaExpression;
import org.quiltmc.chasm.lang.ast.IndexExpression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.ast.SimpleListExpression;
import org.quiltmc.chasm.lang.ast.SimpleMapExpression;
import org.quiltmc.chasm.lang.ast.NullExpression;
import org.quiltmc.chasm.lang.ast.ReferenceExpression;
import org.quiltmc.chasm.lang.ast.StringExpression;
import org.quiltmc.chasm.lang.ast.TernaryExpression;
import org.quiltmc.chasm.lang.ast.UnaryExpression;

public class ChasmExpressionVisitor extends ChasmBaseVisitor<Expression> {
    @Override
    public Expression visitFile(ChasmParser.FileContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitReferenceExpression(ChasmParser.ReferenceExpressionContext ctx) {
        return new ReferenceExpression(ctx,
                ctx.getText(),
                null
        );
    }

    @Override
    public IndexExpression visitMemberExpression(ChasmParser.MemberExpressionContext ctx) {
        return new IndexExpression(ctx,
                ctx.expression().accept(this),
                new StringExpression(ctx.IDENTIFIER(), ctx.IDENTIFIER().getText())
        );
    }

    @Override
    public Expression visitIndexExpression(ChasmParser.IndexExpressionContext ctx) {
        return new IndexExpression(ctx,
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this)
        );
    }

    @Override
    public CallExpression visitCallExpression(ChasmParser.CallExpressionContext ctx) {
        return new CallExpression(ctx,
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this)
        );
    }

    @Override
    public SimpleMapExpression visitMap(ChasmParser.MapContext ctx) {
        Map<String, Expression> entries = new LinkedHashMap<>();

        for (ChasmParser.MapEntryContext entry : ctx.mapEntry()) {
            String key = entry.IDENTIFIER().getText();
            Expression value = entry.expression().accept(this);
            entries.put(key, value);
        }

        return new SimpleMapExpression(ctx, entries);
    }

    @Override
    public SimpleListExpression visitList(ChasmParser.ListContext ctx) {
        List<Expression> entries = new ArrayList<>();

        for (ChasmParser.ExpressionContext entry : ctx.expression()) {
            Expression value = entry.accept(this);
            entries.add(value);
        }

        return new SimpleListExpression(ctx, entries);
    }

    @Override
    public StringExpression visitStringLiteral(ChasmParser.StringLiteralContext ctx) {
        String text = ctx.STRING().getText();
        String inner = text.substring(1, text.length() - 1);

        return new StringExpression(ctx, inner);
    }

    @Override
    public IntegerExpression visitIntegerLiteral(ChasmParser.IntegerLiteralContext ctx) {
        String text = ctx.INTEGER().getText();

        int value;
        if (text.startsWith("0x")) {
            value = Integer.parseInt(text.substring(2, 16));
        } else if (text.startsWith("0b")) {
            value = Integer.parseInt(text.substring(2, 2));
        } else {
            value = Integer.parseInt(text);
        }

        return new IntegerExpression(ctx, value);
    }

    @Override
    public ConstantBooleanExpression visitBooleanLiteral(ChasmParser.BooleanLiteralContext ctx) {
        boolean value = Boolean.parseBoolean(ctx.BOOLEAN().getText());
        return new ConstantBooleanExpression(ctx, value);
    }

    @Override
    public Expression visitNullLiteral(ChasmParser.NullLiteralContext ctx) {
        return new NullExpression(ctx);
    }

    @Override
    public Expression visitGroupExpression(ChasmParser.GroupExpressionContext ctx) {
        return ctx.expression().accept(this);
    }

    @Override
    public Expression visitBinaryExpression(ChasmParser.BinaryExpressionContext ctx) {
        return new BinaryExpression(ctx,
                ctx.expression(0).accept(this),
                BinaryExpression.Operation.fromToken(ctx.op),
                ctx.expression(1).accept(this)
        );
    }

    @Override
    public Expression visitBinaryBooleanExpression(ChasmParser.BinaryBooleanExpressionContext ctx) {
        return new BinaryBooleanExpression(ctx,
                ctx.expression(0).accept(this),
                BinaryBooleanExpression.Operation.fromToken(ctx.op),
                ctx.expression(1).accept(this)
        );
    }

    @Override
    public Expression visitUnaryExpression(ChasmParser.UnaryExpressionContext ctx) {
        return new UnaryExpression(ctx,
                UnaryExpression.Operation.fromToken(ctx.op),
                ctx.expression().accept(this)
        );
    }

    @Override
    public Expression visitTernaryExpression(ChasmParser.TernaryExpressionContext ctx) {
        return new TernaryExpression(ctx,
                ctx.expression(0).accept(this),
                ctx.expression(1).accept(this),
                ctx.expression(2).accept(this)
        );
    }

    @Override
    public LambdaExpression visitLambdaExpression(ChasmParser.LambdaExpressionContext ctx) {
        return new LambdaExpression(ctx,
                ctx.IDENTIFIER().getText(),
                ctx.expression().accept(this),
                null
        );
    }
}
