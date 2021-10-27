import org.antlr.v4.gui.Trees;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;
import org.junit.Test;
import org.quiltmc.chasm.lang.ChasmNodeVisitor;
import org.quiltmc.chasm.lang.ChasmLexer;
import org.quiltmc.chasm.lang.ChasmParser;
import org.quiltmc.chasm.tree.ChasmMap;
import org.quiltmc.chasm.tree.EvaluationContext;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class ChasmFileParseTest {
    @Test
    public void emptyObject() {
        CharStream inputStream = CharStreams.fromString("{}");
        ChasmLexer lexer = new ChasmLexer(inputStream);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ChasmParser parser = new ChasmParser(tokenStream);

        ChasmNodeVisitor visitor = new ChasmNodeVisitor();
        ChasmMap map = visitor.visitMap(parser.map());
    }

    @Test
    public void simpleObject() {
        CharStream inputStream = CharStreams.fromString("""
                {
                    string: "Test",
                    integer: 12345,
                    boolean: false,
                    none: none
                }
                """);
        ChasmLexer lexer = new ChasmLexer(inputStream);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ChasmParser parser = new ChasmParser(tokenStream);

        ChasmNodeVisitor visitor = new ChasmNodeVisitor();
        ChasmMap map = visitor.visitMap(parser.map());
    }

    @Test
    public void exampleFile() throws IOException, ExecutionException, InterruptedException {
        CharStream inputStream = CharStreams.fromStream(Objects.requireNonNull(getClass().getResourceAsStream("example.chasm")));
        ChasmLexer lexer = new ChasmLexer(inputStream);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ChasmParser parser = new ChasmParser(tokenStream);

        ChasmNodeVisitor visitor = new ChasmNodeVisitor();
        ChasmMap map = visitor.visitMap(parser.map());

        ChasmMap evaluated = map.evaluate(new EvaluationContext().with("root", map));

        //Trees.inspect(parser.map(), parser).get().setVisible(true);
        //while (true) {}
    }
}
