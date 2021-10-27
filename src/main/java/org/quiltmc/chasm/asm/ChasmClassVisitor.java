package org.quiltmc.chasm.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.tree.*;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ChasmClassVisitor extends ClassVisitor {
    private final ChasmMap classNode = new ChasmMap();
    private final ChasmList fields = new ChasmList();
    private final ChasmList methods = new ChasmList();

    public ChasmClassVisitor() {
        super(Opcodes.ASM7);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classNode.put("version", new ChasmValue<>(version));
        classNode.put("access", new ChasmValue<>(access));
        classNode.put("name", new ChasmValue<>(name));
        classNode.put("signature", new ChasmValue<>(signature));
        classNode.put("super", new ChasmValue<>(superName));
        classNode.put("interfaces", new ChasmList(Arrays.stream(interfaces).map(ChasmValue::new).collect(Collectors.toList())));

        classNode.put("fields", fields);
        classNode.put("methods", methods);
    }

    @Override
    public void visitSource(String source, String debug) {
        //Stub for completeness
    }

    // Todo: visitModule

    // Todo: visitNestHost

    // Todo: visitOuterClass

    // Todo: visitAnnotation

    // Todo: visitTypeAnnotation

    // Todo: visitAttribute

    // Todo: visitNestMember

    // Todo: visitPermittedSubclass

    // Todo: visitInnerClass

    // Todo: visitRecordComponent

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        ChasmMap fieldNode = new ChasmMap();

        fieldNode.put("access", new ChasmValue<>(access));
        fieldNode.put("name", new ChasmValue<>(name));
        fieldNode.put("descriptor", new ChasmValue<>(descriptor));
        fieldNode.put("signature", new ChasmValue<>(signature));
        fieldNode.put("value", new ChasmValue<>(value));

        return new ChasmFieldVisitor(api, fieldNode);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        ChasmMap methodNode = new ChasmMap();

        methodNode.put("access", new ChasmValue<>(access));
        methodNode.put("name", new ChasmValue<>(name));
        methodNode.put("descriptor", new ChasmValue<>(descriptor));
        methodNode.put("signature", new ChasmValue<>(signature));
        methodNode.put("exceptions", new ChasmList(Arrays.stream(exceptions).map(ChasmValue::new).collect(Collectors.toList())));

        return new ChasmMethodVisitor(api, methodNode);
    }

    // Todo: visitEnd
}
