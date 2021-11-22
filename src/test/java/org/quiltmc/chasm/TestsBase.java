package org.quiltmc.chasm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.Transformer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TestsBase {
    private static final Path TEST_DATA_DIR = Paths.get("src/testData");
    protected final List<TestDefinition> testDefinitions = new ArrayList<>();
    protected ChasmProcessor processor;

    /**
     * The default package used for test classes if the class did not have any.
     */
    protected String getDefaultTestPackage() {
        return "pkg/";
    }

    /**
     * The default package used for transformers if the class did not have any.
     */
    protected String getDefaultTransformerPackage() {
        return "transformer/";
    }

    /**
     * The base package appended to all classes.
     * <p/>
     * I.E. if the test class or transformer is in the package "test/" and the base package is "pkg/",
     * then the full package will be "pkg/test/".
     * If the test class has no package and the default package is "default/", then the full package will be
     * "pkg/default/".
     */
    protected String getBasePackage() {
        return "";
    }

    protected abstract void registerAll();

    protected final void register(String testClass, Transformer... transformers) {
        List<Transformer> transformerList = Arrays.asList(transformers);
        testDefinitions.add(new TestDefinition(getFullTestClassName(testClass), transformerList, new ArrayList<>()));
    }

    protected final void register(String testClass, String[] additionalClasses, Transformer... transformers) {
        List<Transformer> transformerList = Arrays.asList(transformers);
        List<String> additionalClassesList = Arrays.stream(additionalClasses).map(this::getFullTestClassName)
                .collect(Collectors.toList());
        testDefinitions.add(new TestDefinition(testClass, transformerList, additionalClassesList));
    }

    protected final void registerNamed(String testClass, String name, Transformer... transformers) {
        List<Transformer> transformerList = Arrays.asList(transformers);
        testDefinitions.add(new TestDefinition(getFullTestClassName(testClass), transformerList, new ArrayList<>(),
                name));
    }

    protected final void registerNamed(String testClass, String name, String[] additionalClasses,
                                  Transformer... transformers) {
        List<Transformer> transformerList = Arrays.asList(transformers);
        List<String> additionalClassesList = Arrays.stream(additionalClasses).map(this::getFullTestClassName)
                .collect(Collectors.toList());
        testDefinitions.add(new TestDefinition(testClass, transformerList, additionalClassesList, name));
    }

    @BeforeEach
    public void setUp() {
        // Instantiate the processor
        processor = new ChasmProcessor();
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
            DynamicTest test = DynamicTest.dynamicTest(name, Files.exists(resultFile) ? resultFile.toUri() : classFile.toUri(), () -> {
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
        processor.addClass(Files.readAllBytes(classFile));

        // Load any additional classes
        for (String additionalClass : testDefinition.additionalClasses) {
            Path additionalClassFile = TEST_DATA_DIR.resolve(additionalClass + ".class");
            Assertions.assertTrue(Files.isRegularFile(additionalClassFile), additionalClassFile + " does not exist");
            processor.addClass(Files.readAllBytes(additionalClassFile));
        }

        // Add transformers
        for (Transformer transformer : testDefinition.transformers) {
            processor.addTransformer(transformer);
        }

        // Process the data
        List<byte[]> processedClasses = processor.process();

        // Find the result class by name
        ClassReader resultClass = null;
        for (byte[] clazz : processedClasses) {
            // Read basic class info
            resultClass = new ClassReader(clazz);

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

    protected String getFullTestClassName(String className) {
        if (className.indexOf('/') == -1) {
            return getBasePackage() + getDefaultTestPackage() + className;
        }

        return getBasePackage() + className;
    }

    static class TestDefinition {
        public final String testClass;
        public final List<Transformer> transformers;
        public final List<String> additionalClasses;
        private String name;

        public TestDefinition(String testClass, List<Transformer> transformers, List<String> additionalClasses) {
            this.testClass = testClass;
            this.transformers = transformers;
            this.additionalClasses = additionalClasses;
        }

        public TestDefinition(String testClass, List<Transformer> transformers, List<String> additionalClasses,
                              String name) {
            this(testClass, transformers, additionalClasses);
            this.name = name;
        }

        public Path getClassFile() {
            return TEST_DATA_DIR.resolve("classes/" + testClass + ".class");
        }

        public Path getResultFile() {
            return TEST_DATA_DIR.resolve("results/" + getName() + ".result");
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
