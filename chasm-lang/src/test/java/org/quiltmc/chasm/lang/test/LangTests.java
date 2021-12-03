package org.quiltmc.chasm.lang.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.quiltmc.chasm.lang.ChasmLang;
import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.IntegerExpression;
import org.quiltmc.chasm.lang.ast.ListExpression;
import org.quiltmc.chasm.lang.ast.MapExpression;

public class LangTests extends LangTestsBase<LangTests.TestDefinition> {
    @Override
    protected void registerAll() {
        register("parseAndRun", this::testNoop);
        register("brainfuck", this::testBrainfuck);
    }

    private void register(String testFile, TestFunction testFunction) {
        testDefinitions.add(new TestDefinition(testFile, testFunction));
    }

    private void registerNamed(String name, String testFile, TestFunction testFunction) {
        testDefinitions.add(new TestDefinition(testFile, name, testFunction));
    }

    @Override
    protected void doTest(TestDefinition testDefinition) throws IOException {
        // Read the test file
        Path testFile = testDefinition.getTestFile();
        Assertions.assertTrue(Files.isRegularFile(testFile), testFile + " does not exist");
        MapExpression map = ChasmLang.parse(testFile);
        MapExpression reduced = (MapExpression) new ReductionContext().reduce(map);

        // Apply the test function
        testDefinition.applyFunction(reduced);
    }

    private void testBrainfuck(MapExpression map, TestDefinition testDefinition) {
        List<Expression> expressions = ((ListExpression) map.get("result")).getEntries();
        char[] chars = new char[expressions.size()];
        for (int i = 0; i < expressions.size(); i++) {
            IntegerExpression integerExpression = (IntegerExpression) expressions.get(i);
            chars[i] = (char) integerExpression.getValue().intValue();
        }

        String result = String.valueOf(chars);
        Assertions.assertEquals("Hello World!", result);
    }

    private void testNoop(MapExpression map, TestDefinition testDefinition) {
    }

    static class TestDefinition extends BaseTestDefinition {
        private final TestFunction testFunction;

        protected TestDefinition(String testFile, TestFunction testFunction) {
            super(testFile);
            this.testFunction = testFunction;
        }

        protected TestDefinition(String testFile, String name,
                                 TestFunction testFunction) {
            super(testFile, name);
            this.testFunction = testFunction;
        }

        public void applyFunction(MapExpression map) throws IOException {
            testFunction.test(map, this);
        }
    }

    @FunctionalInterface
    interface TestFunction {
        void test(MapExpression map, TestDefinition testDefinition) throws IOException;
    }
}
