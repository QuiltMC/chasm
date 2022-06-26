package org.quiltmc.chasm;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.quiltmc.chasm.api.ChasmProcessor;
import org.quiltmc.chasm.api.ClassData;
import org.quiltmc.chasm.api.util.ClassLoaderClassInfoProvider;
import org.quiltmc.chasm.internal.transformer.ChasmLangTransformer;
import org.quiltmc.chasm.lang.Evaluator;
import org.quiltmc.chasm.lang.Intrinsics;
import org.quiltmc.chasm.lang.op.Expression;

public class LangTests {
    @Test
    public void testLocalIndexes() throws IOException {
        ChasmProcessor processor =
                new ChasmProcessor(new ClassLoaderClassInfoProvider(null, getClass().getClassLoader()));

        byte[] classBytes = getClass().getResourceAsStream("other.TestLocalVariables.class").readAllBytes();
        processor.addClass(new ClassData(classBytes));

        // TODO: remove len when there is a builtin length function
        String transformerString = """
                """;

        Evaluator evaluator = new Evaluator();
        evaluator.getScope().push(Intrinsics.SCOPE);
        Expression parsed = Expression.parse(CharStreams.fromString(transformerString));
        ChasmLangTransformer transformer = new ChasmLangTransformer(evaluator, parsed);
        processor.addTransformer(transformer);

        List<ClassData> classes = processor.process();

        ClassReader reader = new ClassReader(classes.get(0).getClassBytes());
        StringWriter resultString = new StringWriter();
        TraceClassVisitor resultVisitor = new TraceClassVisitor(new PrintWriter(resultString));
        reader.accept(resultVisitor, 0);

        System.out.println(resultString);
    }
}
