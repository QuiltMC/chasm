package org.quiltmc.chasm.lang;

import java.io.IOException;
import java.nio.file.Path;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.quiltmc.chasm.lang.antlr.ChasmLexer;
import org.quiltmc.chasm.lang.antlr.ChasmParser;
import org.quiltmc.chasm.lang.ast.MapExpression;
import org.quiltmc.chasm.lang.visitor.ChasmExpressionVisitor;

public class ChasmLang {
    public static MapExpression parse(String string) {
        return parse(CharStreams.fromString(string));
    }

    public static MapExpression parse(Path path) throws IOException {
        return parse(CharStreams.fromPath(path));
    }

    public static MapExpression parse(CharStream charStream) {
        ChasmLexer chasmLexer = new ChasmLexer(charStream);
        TokenStream tokenStream = new CommonTokenStream(chasmLexer);
        ChasmParser chasmParser = new ChasmParser(tokenStream);

        ChasmParser.FileContext fileContext = chasmParser.file();
        ChasmExpressionVisitor mapVisitor = new ChasmExpressionVisitor();
        MapExpression mapExpression = mapVisitor.visitMap(fileContext.map());
        mapExpression.resolve("$", mapExpression);
        return mapExpression;
    }
}
