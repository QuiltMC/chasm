package org.quiltmc.chasm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.transformer.Transformer;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TestsBase {
    private static final Path TEST_DATA_DIR = Paths.get("testData");
    protected final List<TestDefinition> testDefinitions = new ArrayList<>();
    protected URLClassLoader classLoader;
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

    protected final void register(String testClass, String... transformers) {
        List<String> transformerList = Arrays.stream(transformers).map(this::getFullTransformerClassName)
                .collect(Collectors.toList());
        testDefinitions.add(new TestDefinition(getFullTestClassName(testClass), transformerList, new ArrayList<>()));
    }

    protected final void register(String testClass, String[] additionalClasses, String... transformers) {
        List<String> transformerList = Arrays.stream(transformers).map(this::getFullTransformerClassName)
                .collect(Collectors.toList());
        List<String> additionalClassesList = Arrays.stream(additionalClasses).map(this::getFullTestClassName)
                .collect(Collectors.toList());
        testDefinitions.add(new TestDefinition(testClass, transformerList, additionalClassesList));
    }

    @BeforeEach
    public void setUp() throws MalformedURLException {
        // Instantiate the processor
        processor = new ChasmProcessor();

        // Instantiate a class loader for the transformers
        if (classLoader == null) {
            classLoader = new URLClassLoader(new URL[] {TEST_DATA_DIR.resolve("classes").toUri().toURL()});
        }
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
            // Get the test name
            String name = testDefinition.testClass;
            if (name.indexOf('/') != -1) {
                name = name.substring(name.lastIndexOf('/') + 1);
            }

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

        // Load and instantiate transformers
        for (String transformerClass : testDefinition.transformers) {
            Path transformerFile = TEST_DATA_DIR.resolve("classes/" + transformerClass + ".class");
            Assertions.assertTrue(Files.isRegularFile(transformerFile), transformerFile + " does not exist");

            try {
                // Load the transformer class with a URLClassLoader
                Class<?> clazz = classLoader.loadClass(transformerClass.replace('/', '.'));
                // Check that the transformer implements Transformer
                Assertions.assertTrue(Transformer.class.isAssignableFrom(clazz), transformerClass + " is not a Transformer");
                Transformer transformer = (Transformer) clazz.getConstructor().newInstance();
                processor.addTransformer(transformer);
            } catch (ReflectiveOperationException e) {
                // Fail under any reflective operation exception
                Assertions.fail(e);
            }
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
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        resultClass.accept(resultVisitor, 0);

        // Load the reference file
        Path referenceFile = testDefinition.getResultFile();

        // If it doesn't exist, create it and skip the test
        if (!Files.exists(referenceFile)) {
            // Write result to file
            Files.createDirectories(referenceFile.getParent());
            Files.writeString(referenceFile, resultString.toString());

            //noinspection ConstantConditions - Using Assumptions.assumeTrue() to abort the test instead of failing it
            Assumptions.assumeTrue(false, "Reference file " + referenceFile + " did not exist");
        } else {
            Assertions.assertTrue(Files.isRegularFile(referenceFile), referenceFile + " does not exist");

            // Assert that the result has the same content as expected
            String expectedContent = Files.readString(referenceFile).replace("\r\n", "\n");
            Assertions.assertEquals(expectedContent, resultString.toString());
        }
    }

    protected String getFullTestClassName(String className) {
        if (className.indexOf('/') == -1) {
            return getBasePackage() + getDefaultTestPackage() + className;
        }

        return getBasePackage() + className;
    }

    protected String getFullTransformerClassName(String className) {
        if (className.indexOf('/') == -1) {
            return getBasePackage() + getDefaultTransformerPackage() + className;
        }

        return getBasePackage() + className;
    }

    @SuppressWarnings("ClassCanBeRecord")
    static class TestDefinition {
        public final String testClass;
        public final List<String> transformers;
        public final List<String> additionalClasses;

        public TestDefinition(String testClass, List<String> transformers, List<String> additionalClasses) {
            this.testClass = testClass;
            this.transformers = transformers;
            this.additionalClasses = additionalClasses;
        }

        public Path getClassFile() {
            return TEST_DATA_DIR.resolve("classes/" + testClass + ".class");
        }

        public Path getResultFile() {
            return TEST_DATA_DIR.resolve("results/" + testClass + ".result");
        }
    }
}
