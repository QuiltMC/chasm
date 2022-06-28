package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.antlr.ChasmLexer;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.visitor.ChasmExpressionVisitor;

public abstract class AbstractExpression implements Expression {
    private final ParseTree tree;

    public AbstractExpression(ParseTree tree) {
        this.tree = tree;
    }

    public ParseTree getParseTree() {
        return tree;
    }

    public abstract Expression resolve(ScopeStack scope);

    public abstract Expression reduce(Cache cache);

    @Override
    public String toString() {
        if (tree != null) {
            return tree.getText();
        } else {
            return super.toString();
        }
    }
}
