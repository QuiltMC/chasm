package org.quiltmc.chasm.lang.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.util.ClassLoaderSuperClassProvider;
import org.quiltmc.chasm.lang.ChasmLangTransformer;

public class LangTransformerTests extends LangTestsBase<LangTransformerTests.TestDefinition> {
    private ChasmProcessor processor;

    @Override
    protected void registerAll() {
        register("exampleTransformer", "pkg/TestClass");
    }

    private void register(String testFile, String targetClass) {
        testDefinitions.add(new TestDefinition(testFile, targetClass, new ArrayList<>()));
    }

    private void register(String testFile, String targetClass, String... additionalClasses) {
        List<String> additionalClassesList = Arrays.asList(additionalClasses);
        testDefinitions.add(new TestDefinition(testFile, targetClass, additionalClassesList));
    }

    private void register(String testFile, String targetClass, List<String> additionalClasses,
                          List<String> additionalTransformers) {
        testDefinitions.add(new TestDefinition(testFile, targetClass, additionalClasses, additionalTransformers));
    }

    private void registerNamed(String name, String testFile, String targetClass) {
        testDefinitions.add(new TestDefinition(testFile, name, targetClass, new ArrayList<>()));
    }

    private void registerNamed(String name, String testFile, String targetClass, String... additionalClasses) {
        List<String> additionalClassesList = Arrays.asList(additionalClasses);
        testDefinitions.add(new TestDefinition(testFile, name, targetClass, additionalClassesList));
    }

    private void registerNamed(String name, String testFile, String targetClass, List<String> additionalClasses,
                               List<String> additionalTransformers) {
        testDefinitions.add(new TestDefinition(testFile, name, targetClass, additionalClasses, additionalTransformers));
    }

    @BeforeEach
    public void setUp() {
        // Instantiate the processor
        processor = new ChasmProcessor(new ClassLoaderSuperClassProvider(null, getClass().getClassLoader()));
    }

    @AfterEach
    public void tearDown() {
        processor = null;
    }

    @Override
    protected void doTest(TestDefinition testDefinition) throws IOException {
        // Load the target class
        Path targetClass = getTestClass(testDefinition.targetClass);
        Assertions.assertTrue(Files.isRegularFile(targetClass), targetClass + " does not exist");
        processor.addClass(Files.readAllBytes(targetClass));

        // Load the additional classes
        for (String additionalClass : testDefinition.additionalClasses) {
            Path additionalClassFile = getTestClass(additionalClass);
            Assertions.assertTrue(Files.isRegularFile(additionalClassFile), additionalClassFile + " does not exist");
            processor.addClass(Files.readAllBytes(additionalClassFile));
        }

        // Create and load the transformer
        Path transformerFile = testDefinition.getTestFile();
        Assertions.assertTrue(Files.isRegularFile(transformerFile), transformerFile + " does not exist");
        ChasmLangTransformer transformer = ChasmLangTransformer.parse(transformerFile);
        processor.addTransformer(transformer);

        // Create and load the additional transformers
        for (String additionalTransformer : testDefinition.additionalTransformers) {
            Path additionalTransformerFile = BaseTestDefinition.getTestFile(additionalTransformer);
            Assertions.assertTrue(Files.isRegularFile(additionalTransformerFile),
                    additionalTransformerFile + " does not exist");
            ChasmLangTransformer transformer2 = ChasmLangTransformer.parse(additionalTransformerFile);
            processor.addTransformer(transformer2);
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
            if (binaryName.equals(testDefinition.targetClass)) {
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

    static class TestDefinition extends BaseTestDefinition {
        public final String targetClass;
        public final List<String> additionalClasses;
        public final List<String> additionalTransformers;

        protected TestDefinition(String testFile, String targetClass, List<String> additionalClasses) {
            this(testFile, targetClass, additionalClasses, new ArrayList<>());
        }

        protected TestDefinition(String testFile, String targetClass, List<String> additionalClasses,
                                 List<String> additionalTransformers) {
            super(testFile);
            this.targetClass = targetClass;
            this.additionalClasses = additionalClasses;
            this.additionalTransformers = additionalTransformers;
        }

        protected TestDefinition(String testFile, String name, String targetClass, List<String> additionalClasses) {
            this(testFile, name, targetClass, additionalClasses, new ArrayList<>());
        }

        protected TestDefinition(String testFile, String name, String targetClass, List<String> additionalClasses,
                                 List<String> additionalTransformers) {
            super(testFile, name);
            this.targetClass = targetClass;
            this.additionalClasses = additionalClasses;
            this.additionalTransformers = additionalTransformers;
        }
    }
}
