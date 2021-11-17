package org.quiltmc.chasm;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.quiltmc.chasm.asm.ChasmClassVisitor;
import org.quiltmc.chasm.asm.ChasmClassWriter;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.NodePrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BasicClassTests {
    @Test
    public void parseExampleClass() throws IOException {
        ClassReader reader = new ClassReader("org.quiltmc.chasm.ExampleClass");
        ChasmClassVisitor classVisitor = new ChasmClassVisitor();
        reader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        MapNode classNode = classVisitor.getClassNode();

        NodePrinter printer = new NodePrinter(System.out);
        printer.print(classNode);
    }

    @Test
    void parseAndPrintExampleClass() throws IOException {
        ClassReader reader = new ClassReader("org.quiltmc.chasm.ExampleClass");
        ChasmClassVisitor classVisitor = new ChasmClassVisitor();
        reader.accept(classVisitor, ClassReader.SKIP_FRAMES);
        MapNode classNode = classVisitor.getClassNode();

        Path outTreePath = Paths.get("ExampleClass.json");
        OutputStream treeStream = Files.newOutputStream(outTreePath);
        PrintStream treePrintStream = new PrintStream(treeStream);
        NodePrinter printer = new NodePrinter(treePrintStream);
        printer.print(classNode);
        treePrintStream.flush();

        ChasmClassWriter writer = new ChasmClassWriter(classNode);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        writer.accept(classWriter);

        Path outClassPath = Paths.get("ExampleClass.class");
        OutputStream classStream = Files.newOutputStream(outClassPath);
        classStream.write(classWriter.toByteArray());
    }
}
