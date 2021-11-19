package org.quiltmc.chasm;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.asm.ChasmClassVisitor;
import org.quiltmc.chasm.asm.writer.ChasmClassWriter;
import org.quiltmc.chasm.transformer.Transformer;
import org.quiltmc.chasm.tree.MapNode;
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
    public void checkClassIdentity(Class<?> targetClass)
            throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        ChasmProcessor chasmProcessor = new ChasmProcessor();

        InputStream classStream = targetClass.getResourceAsStream(targetClass.getSimpleName() + ".class");
        byte[] originalClassBytes = classStream.readAllBytes();;
        chasmProcessor.addClass(originalClassBytes);

        CheckTransformed annotation = targetClass.getAnnotation(CheckTransformed.class);

        for (Class<?> clazz : annotation.classes()) {
            InputStream stream = targetClass.getResourceAsStream(targetClass.getSimpleName() + ".class");
            chasmProcessor.addClass(stream.readAllBytes());
        }

        for (Class<? extends Transformer> clazz : annotation.transformer()) {
            Transformer transformer = clazz.getConstructor().newInstance();
            chasmProcessor.addTransformer(transformer);
        }

        List<byte[]> classBytes = chasmProcessor.process();
        ClassReader resultClass = null;
        for (byte[] clazz : classBytes) {
            resultClass = new ClassReader(clazz);
            if (resultClass.getClassName().replace('/', '.').replace('$', '.').equals(targetClass.getName())) {
                break;
            }
        }

        Assertions.assertNotNull(resultClass);

        // Write class into string
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        resultClass.accept(resultVisitor, 0);

        Class<?> referenceClass = annotation.result();
        InputStream referenceClassStream = referenceClass.getResourceAsStream(referenceClass.getSimpleName() + ".class");
        ClassReader referenceReader = new ClassReader(originalClassBytes);

        // Pass class through ASM (for equal frames)
        ClassWriter referenceClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        referenceReader.accept(referenceClassWriter, 0);
        referenceReader = new ClassReader(referenceClassWriter.toByteArray());

        // Write original class into string
        StringWriter referenceString = new StringWriter();
        TraceClassVisitor referenceVisitor = new TraceClassVisitor(new PrintWriter(referenceString));
        referenceReader.accept(referenceVisitor, 0);

        Assertions.assertEquals(referenceString.toString(), resultString.toString());
    }
}
