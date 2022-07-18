import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import org.quiltmc.chasm.lang.Evaluator;
import org.quiltmc.chasm.lang.ast.AbstractListExpression;
import org.quiltmc.chasm.lang.ast.AbstractMapExpression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.op.Expression;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        var length = ((AbstractMapExpression) reduced).get("length");
        assertInstanceOf(IntegerExpression.class, length);
        assertEquals(6, ((IntegerExpression) length).getValue());
    }

    @Test
    public void lenNonListTest() {
        String test = """
                {
                    not_a_list: 0,
                    length: len(not_a_list)
                }
                """;

        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        assertThrows(RuntimeException.class, () -> {
            Expression resolved = evaluator.resolve(parsed);
            Expression reduced = evaluator.reduceRecursive(resolved);
        });
    }

    @Test
    public void flattenTest() {
        String test = """
                {
                    list: [[0], [1, 2, 3], [100, 10, 3], ["hello!", "world!"], [4], [[5], [6, 7, 8]]],
                    flattened: flatten(list)
                }
                """;
        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        Expression resolved = evaluator.resolve(parsed);
        Expression reduced = evaluator.reduceRecursive(resolved);

        assertInstanceOf(AbstractMapExpression.class, reduced);
        var flattened = ((AbstractMapExpression) reduced).get("flattened");
        assertInstanceOf(AbstractListExpression.class, flattened);
        var flattenedCasted = ((AbstractListExpression) flattened);
        assertEquals("[0, 1, 2, 3, 100, 10, 3, hello!, world!, 4, [5], [6, 7, 8]]", flattenedCasted.toString());
    }

    @Test
    public void flattenNonListTest() {
        String test = """
                {
                    not_a_list: 0,
                    flattened: flatten(not_a_list)
                }
                """;

        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        assertThrows(RuntimeException.class, () -> {
            Expression resolved = evaluator.resolve(parsed);
            Expression reduced = evaluator.reduceRecursive(resolved);
        });
    }

    @Test
    public void flattenNonListOfListsTest() {
        String test = """
                {
                    a_list_of_non_lists: [0, 1, 2],
                    flattened: flatten(a_list_of_non_lists)
                }
                """;

        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        assertThrows(RuntimeException.class, () -> {
            Expression resolved = evaluator.resolve(parsed);
            Expression reduced = evaluator.reduceRecursive(resolved);
        });
    }

    @Test
    public void mapTest() {
        String test = """
                {
                    mapped: map({list: [0, 1, 2, 3, 4], function: val -> val + 1})
                }
                """;
        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        Expression resolved = evaluator.resolve(parsed);
        Expression reduced = evaluator.reduceRecursive(resolved);

        assertInstanceOf(AbstractMapExpression.class, reduced);
        var mapped = ((AbstractMapExpression) reduced).get("mapped");
        assertInstanceOf(AbstractListExpression.class, mapped);
        var mappedCasted = ((AbstractListExpression) mapped);
        assertEquals("[1, 2, 3, 4, 5]", mappedCasted.toString());
    }

    @Test
    public void mapNonListTest() {
        String test = """
                {
                    mapped: map({list: 0, function: val -> val + 1})
                }
                """;
        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        assertThrows(RuntimeException.class, () -> {
            Expression resolved = evaluator.resolve(parsed);
            Expression reduced = evaluator.reduceRecursive(resolved);
        });
    }

    @Test
    public void mapNonFunctionTest() {
        String test = """
                {
                    mapped: map({list: [0], function: 0})
                }
                """;
        Evaluator evaluator = new Evaluator();
        Expression parsed = Expression.parse(CharStreams.fromString(test));
        assertThrows(RuntimeException.class, () -> {
            Expression resolved = evaluator.resolve(parsed);
            Expression reduced = evaluator.reduceRecursive(resolved);
        });
    }
}
