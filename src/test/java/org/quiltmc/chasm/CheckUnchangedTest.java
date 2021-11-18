package org.quiltmc.chasm;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.asm.ChasmClassVisitor;
import org.quiltmc.chasm.asm.writer.ChasmClassWriter;
import org.quiltmc.chasm.tree.MapNode;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public class CheckUnchangedTest {
    public static String[] classNames() {
        Reflections reflections = new Reflections("org.quiltmc.chasm.tests", Scanners.TypesAnnotated);
        Set<Class<?>> classes = reflections.get(Scanners.TypesAnnotated.with(CheckUnchanged.class).asClass());
        return classes.stream().map(Class::getName).toArray(String[]::new);
    }

    @ParameterizedTest
    @MethodSource("classNames")
    public void checkClassIdentity(String className) throws IOException {
        // Create class reader
        ClassReader reader = new ClassReader(className);

        // Parse class
        ChasmClassVisitor classVisitor = new ChasmClassVisitor();
        reader.accept(classVisitor, 0);
        MapNode mapNode = classVisitor.getClassNode();

        // Write class
        ChasmClassWriter resultWriter = new ChasmClassWriter(mapNode);
        ClassWriter resultClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        resultWriter.accept(resultClassWriter);
        ClassReader resultReader = new ClassReader(resultClassWriter.toByteArray());

        // Write class into string
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        resultReader.accept(resultVisitor, 0);

        // Pass class through ASM (for equal frames)
        ClassWriter referenceClassWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        reader.accept(referenceClassWriter, 0);
        ClassReader referenceReader = new ClassReader(referenceClassWriter.toByteArray());

        // Write original class into string
        StringWriter referenceString = new StringWriter();
        TraceClassVisitor referenceVisitor = new TraceClassVisitor(new PrintWriter(referenceString));
        referenceReader.accept(referenceVisitor, 0);

        // Class shouldn't have changed
        Assertions.assertEquals(referenceString.toString(), resultString.toString());
    }
}
