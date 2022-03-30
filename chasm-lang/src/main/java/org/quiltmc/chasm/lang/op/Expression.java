package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.antlr.ChasmLexer;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.visitor.ChasmExpressionVisitor;

public interface Expression {
    ParseTree getParseTree();

    Expression resolve(ScopeStack scope);

    Expression reduce(Cache cache);

    static Expression parse(CharStream charStream) {
        ChasmLexer lexer = new ChasmLexer(charStream);
        TokenStream tokens = new CommonTokenStream(lexer);
        ChasmParser parser = new ChasmParser(tokens);
        ChasmExpressionVisitor visitor = new ChasmExpressionVisitor();

        //parser.setErrorHandler(new BailErrorStrategy());
        return parser.file().accept(visitor);
    }
}
