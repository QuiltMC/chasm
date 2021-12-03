package org.quiltmc.chasm.lang.test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public abstract class LangTestsBase<T extends LangTestsBase.BaseTestDefinition> {
    private static final Path TEST_CLASSES_DIR = Paths.get("build/classes/java/testData");
    private static final Path TEST_FILES_DIR = Paths.get("src/testData/tests");
    private static final Path TEST_RESULTS_DIR = Paths.get("src/testData/results");
    protected final List<T> testDefinitions = new ArrayList<>();

    protected abstract void registerAll();

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    protected abstract void doTest(T testDefinition) throws IOException;

    @TestFactory
    public Stream<DynamicTest> testRegistered() {
        // Register all the tests
        testDefinitions.clear();
        registerAll();

        // Create a test for each test definition
        List<DynamicTest> tests = new ArrayList<>();
        for (T testDefinition : testDefinitions) {
            String name = testDefinition.getName();

            // Create the test
            Path testFile = testDefinition.getTestFile();
            URI testSourceUri = testFile.toUri();
            DynamicTest test = DynamicTest.dynamicTest(name, testSourceUri, () -> {
                setUp();
                doTest(testDefinition);
                tearDown();
            });

            tests.add(test);
        }

        return tests.stream();
    }

    protected static Path getTestClass(String className) {
        return TEST_CLASSES_DIR.resolve(className + ".class");
    }

    // Each test set has different needs, so each one should implement their own test definition
    abstract static class BaseTestDefinition {
        public final String testFile;
        private String name;

        protected BaseTestDefinition(String testFile) {
            this.testFile = testFile;
        }

        protected BaseTestDefinition(String testFile, String name) {
            this.testFile = testFile;
            this.name = name;
        }

        public Path getTestFile() {
            return TEST_FILES_DIR.resolve(testFile + ".chasm");
        }

        public Path getResultFile() {
            return TEST_RESULTS_DIR.resolve(getName() + ".result");
        }

        public String getName() {
            if (name != null) {
                return name;
            }

            String name = testFile;
            if (name.indexOf('/') != -1) {
                name = name.substring(name.lastIndexOf('/') + 1);
            }

            return name;
        }
    }
}
