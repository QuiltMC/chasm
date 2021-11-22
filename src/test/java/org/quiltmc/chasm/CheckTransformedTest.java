package org.quiltmc.chasm;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.Transformer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public class CheckTransformedTest {
    public static Class<?>[] getClasses() {
        Reflections reflections = new Reflections("org.quiltmc.chasm.tests", Scanners.TypesAnnotated);
        Set<Class<?>> classes = reflections.get(Scanners.TypesAnnotated.with(CheckTransformed.class).asClass());
        return classes.toArray(Class[]::new);
    }

    @ParameterizedTest
    @MethodSource("getClasses")
    public void checkClassIdentity(Class<?> targetClass) throws Exception {
        // Get the annotation
        CheckTransformed annotation = targetClass.getAnnotation(CheckTransformed.class);

        // Instantiate the processor
        ChasmProcessor chasmProcessor = new ChasmProcessor();

        // Load target class
        InputStream targetClassStream = targetClass.getResourceAsStream(targetClass.getSimpleName() + ".class");
        assert targetClassStream != null;
        chasmProcessor.addClass(targetClassStream.readAllBytes());

        // Load and instantiate transformers
        for (Class<? extends Transformer> clazz : annotation.transformer()) {
            Transformer transformer = clazz.getConstructor().newInstance();
            chasmProcessor.addTransformer(transformer);
        }

        // Load additional classes
        for (Class<?> clazz : annotation.classes()) {
            InputStream additionalClassStream = targetClass.getResourceAsStream(clazz.getSimpleName() + ".class");
            assert additionalClassStream != null;
            chasmProcessor.addClass(additionalClassStream.readAllBytes());
        }

        // Process the data
        List<byte[]> classBytes = chasmProcessor.process();

        // Find the result class by name
        ClassReader resultClass = null;
        for (byte[] clazz : classBytes) {
            // Read basic class info
            resultClass = new ClassReader(clazz);

            // Convert the JVM binary name (e.g. org/example/Class$Inner) into
            // the JLS binary name (e.g. org.example.Class$Inner)
            String binaryName = resultClass.getClassName().replace('/', '.');
            if (binaryName.equals(targetClass.getName())) {
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
        String expectedFile = annotation.expected();
        InputStream expectedStream = targetClass.getResourceAsStream(expectedFile);

        // If it doesn't exist, create it and fail the test
        if (expectedStream == null) {
            // Get file location
            Path dir = Paths.get("src/test/resources", targetClass.getPackageName().replace('.', '/'));
            Path expectedPath = dir.resolve(expectedFile);

            // Write result to file
            Files.createDirectories(expectedPath.getParent());
            Files.writeString(expectedPath, resultString.toString());
            Assertions.fail("Created missing expected result: " + expectedFile);
        } else {
            // Read string from stream
            ByteArrayOutputStream expectedBytes = new ByteArrayOutputStream();
            expectedStream.transferTo(expectedBytes);

            // Assert that the result has the same content as expected
            Assertions.assertEquals(expectedBytes.toString(), resultString.toString());
        }
    }
}
