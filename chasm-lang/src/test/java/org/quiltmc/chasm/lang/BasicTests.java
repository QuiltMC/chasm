package org.quiltmc.chasm.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.quiltmc.chasm.lang.api.ast.Expression;
import org.quiltmc.chasm.lang.api.eval.Evaluator;

public class BasicTests {
    private static final Path TESTS_DIR = Paths.get("src/test/resources/tests");
    private static final Path RESULTS_DIR = Paths.get("src/test/resources/results");

    @TestFactory
    public Stream<DynamicTest> testAll() {
        // Create a test for each test definition
        List<DynamicTest> tests = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(TESTS_DIR)) {
            paths.forEach(path -> {
                if (Files.isRegularFile(path)) {
                    Path relative = TESTS_DIR.relativize(path);
                    String name = relative.toString();

                    DynamicTest test = DynamicTest.dynamicTest(name, () -> doTest(relative));

                    tests.add(test);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return tests.stream();
    }

    private void doTest(Path path) throws IOException {
        Path testPath = TESTS_DIR.resolve(path);
        Path resultPath = RESULTS_DIR.resolve(path);

        Expression parsed = Expression.read(testPath);
        Expression result = Evaluator.create().evaluate(parsed);
        String rendered = result.render();

        // If result doesn't exist yet, create file but fail anyway
        if (!Files.exists(resultPath)) {
            Files.createDirectories(resultPath.getParent());
            Files.write(resultPath, rendered.getBytes());
            Assertions.fail("Automatically created result file. Please verify results before committing.");
        }

        String expected = Files.readString(resultPath);
        Assertions.assertEquals(expected, rendered);
    }
}
