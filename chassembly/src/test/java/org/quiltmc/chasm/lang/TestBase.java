package org.quiltmc.chasm.lang;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public abstract class TestBase {
    private static final Path TESTS_DIR = Paths.get("src/test/resources/tests");
    private static final Path RESULTS_DIR = Paths.get("src/test/resources/results");

    @TestFactory
    public Stream<DynamicTest> testAll() {
        // Create a test for each test definition
        List<DynamicTest> tests = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(TESTS_DIR)) {
            paths.forEach(path -> {
                if (Files.isRegularFile(path) && path.toString().endsWith(".chasm")) {
                    Path relative = TESTS_DIR.relativize(path);
                    String name = relative.toString();

                    Path resultPath = RESULTS_DIR.resolve(relative);
                    DynamicTest test = DynamicTest.dynamicTest(name, () ->  {
                        long start = System.nanoTime();
                        doTest(path, resultPath);
                        long end = System.nanoTime();
                        double time = (end - start) * 1e-6;
                        System.out.printf(Locale.US, "%s: %.2fms\n", name, time);
                    });

                    tests.add(test);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return tests.stream();
    }

    protected abstract void doTest(Path testPath, Path resultPath) throws IOException;
}
