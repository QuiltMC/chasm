import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import org.quiltmc.chasm.lang.Evaluator;
import org.quiltmc.chasm.lang.ast.AbstractMapExpression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.op.Expression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class IntrinsicsTest {
    @Test
    public void lenTest() {
        String test = """
                {
                    list: [0, 1, 2, 3, 4, 5],
                    length: len(list)
                }
                """;
        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        Expression resolved = evaluator.resolve(parsed);
        Expression reduced = evaluator.reduceRecursive(resolved);

        assertInstanceOf(AbstractMapExpression.class, reduced);
        var length = ((AbstractMapExpression)reduced).get("length");
        assertInstanceOf(IntegerExpression.class, length);
        assertEquals(6, ((IntegerExpression)length).getValue());
    }
}
