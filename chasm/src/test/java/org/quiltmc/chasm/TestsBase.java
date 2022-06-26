package org.quiltmc.chasm;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.ClassData;
import org.quiltmc.chasm.api.Transformer;
import org.quiltmc.chasm.api.util.ClassLoaderClassInfoProvider;
import org.quiltmc.chasm.internal.transformer.ChasmLangTransformer;
import org.quiltmc.chasm.lang.Evaluator;
import org.quiltmc.chasm.lang.Intrinsics;
import org.quiltmc.chasm.lang.op.Expression;

public abstract class TestsBase {
    private static final Path TEST_CLASSES_DIR = Paths.get("build/classes/java/testData");
    private static final Path TEST_RESULTS_DIR = Paths.get("src/testData/results");
    private static final Path TEST_TRANSFORMERS_DIR = Paths.get("src/testData/transformers");

    protected final List<TestDefinition> testDefinitions = new ArrayList<>();
    protected ChasmProcessor processor;

    protected abstract void registerAll();

    protected final void register(String testClass, String name, String... transformers) {
        List<String> transformerList = Arrays.asList(transformers);
        testDefinitions.add(new TestDefinition(testClass, transformerList, new ArrayList<>(),
                name));
    }

    protected final void register(String testClass, String name, String[] additionalClasses, String... transformers) {
        List<String> transformerList = Arrays.asList(transformers);
        List<String> additionalClassesList = Arrays.stream(additionalClasses).collect(Collectors.toList());
        testDefinitions.add(new TestDefinition(testClass, transformerList, additionalClassesList, name));
    }

    @BeforeEach
    public void setUp() {
        // Instantiate the processor
        processor = new ChasmProcessor(new ClassLoaderClassInfoProvider(null, getClass().getClassLoader()));
    }

    @AfterEach
    public void tearDown() {
        processor = null;
    }

    @TestFactory
    public Stream<DynamicTest> testRegistered() {
        // Register all the tests
        testDefinitions.clear();
        registerAll();

        // Create a test for each test definition
        List<DynamicTest> tests = new ArrayList<>();
        for (TestDefinition testDefinition : testDefinitions) {
            String name = testDefinition.getName();

            // Create the test
            Path classFile = testDefinition.getClassFile();
            Path resultFile = testDefinition.getResultFile();
            URI testSourceUri = Files.exists(resultFile) ? resultFile.toUri() : classFile.toUri();
            DynamicTest test = DynamicTest.dynamicTest(name, testSourceUri, () -> {
                setUp();
                doTest(testDefinition);
                tearDown();
            });

            tests.add(test);
        }

        return tests.stream();
    }

    protected void doTest(TestDefinition testDefinition) throws IOException {
        // Load the test class
        Path classFile = testDefinition.getClassFile();
        Assertions.assertTrue(Files.isRegularFile(classFile), classFile + " does not exist");
        processor.addClass(new ClassData(Files.readAllBytes(classFile)));

        // Load any additional classes
        for (String additionalClass : testDefinition.additionalClasses) {
            Path additionalClassFile = TEST_CLASSES_DIR.resolve(additionalClass + ".class");
            Assertions.assertTrue(Files.isRegularFile(additionalClassFile), additionalClassFile + " does not exist");
            processor.addClass(new ClassData(Files.readAllBytes(additionalClassFile)));
        }

        Evaluator evaluator = new Evaluator();

        // Add transformers
        for (String transformer : testDefinition.transformers) {
            Path transformerFile = TEST_TRANSFORMERS_DIR.resolve(transformer + ".chasm");
            Assertions.assertTrue(Files.isRegularFile(transformerFile), transformerFile + " does not exist");
            Expression expression = Expression.parse(CharStreams.fromPath(transformerFile));
            processor.addTransformer(new ChasmLangTransformer(evaluator, expression));
        }

        // Process the data
        List<ClassData> processedClasses = processor.process();

        // Find the result class by name
        ClassReader resultClass = null;
        for (ClassData classData : processedClasses) {
            // Read basic class info
            resultClass = new ClassReader(classData.getClassBytes());

            // Convert the JVM binary name (e.g. org/example/Class$Inner) into
            // the JLS binary name (e.g. org.example.Class$Inner)
            String binaryName = resultClass.getClassName().replace('/', '.');
            if (binaryName.equals(testDefinition.testClass)) {
                break;
            }
        }

        // Assert that the result class was found
        Assertions.assertNotNull(resultClass);

        // Write class into string
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString) {
            @Override
            public void println() {
                // Always use unix-style line endings
                write('\n');
            }
        });
        resultClass.accept(resultVisitor, 0);

        // Load the reference file
        Path referenceFile = testDefinition.getResultFile();

        // If it doesn't exist, create it and skip the test
        if (!Files.exists(referenceFile)) {
            // Write result to file
            Files.createDirectories(referenceFile.getParent());
            Files.writeString(referenceFile, resultString.toString());

            Assertions.fail("Reference file " + referenceFile + " did not exist");
        } else {
            Assertions.assertTrue(Files.isRegularFile(referenceFile), referenceFile + " does not exist");

            // Assert that the result has the same content as expected
            Assertions.assertEquals(Files.readString(referenceFile), resultString.toString());
        }
    }

    static class TestDefinition {
        public final String testClass;
        public final List<String> transformers;
        public final List<String> additionalClasses;
        private String name;

        public TestDefinition(String testClass, List<String> transformers, List<String> additionalClasses) {
            this.testClass = testClass;
            this.transformers = transformers;
            this.additionalClasses = additionalClasses;
        }

        public TestDefinition(String testClass, List<String> transformers, List<String> additionalClasses,
                              String name) {
            this(testClass, transformers, additionalClasses);
            this.name = name;
        }

        public Path getClassFile() {
            return TEST_CLASSES_DIR.resolve(testClass + ".class");
        }

        public Path getResultFile() {
            return TEST_RESULTS_DIR.resolve(getName() + ".result");
        }

        public String getName() {
            if (name != null) {
                return name;
            }

            String name = testClass;
            if (name.indexOf('/') != -1) {
                name = name.substring(name.lastIndexOf('/') + 1);
            }

            return name;
        }
    }
}
