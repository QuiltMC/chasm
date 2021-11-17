package org.quiltmc.chasm.asm;

import org.objectweb.asm.*;
import org.quiltmc.chasm.tree.*;

public class ChasmClassVisitor extends ClassVisitor {
    private final MapNode classNode = new LinkedHashMapNode();
    private final ListNode fields = new LinkedListNode();
    private final ListNode methods = new LinkedListNode();
    private final ListNode recordComponents = new LinkedListNode();

    private final ListNode nestMembers = new LinkedListNode();
    private final ListNode permittedSubclasses = new LinkedListNode();
    private final ListNode innerClasses = new LinkedListNode();

    private final ListNode annotations = new LinkedListNode();
    private final ListNode attributes = new LinkedListNode();

    public ChasmClassVisitor() {
        super(Opcodes.ASM7);
    }

    public MapNode getClassNode() {
        return classNode;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // NOTE: Ensure parity with names in ClassNode
        classNode.put("version", new ValueNode<>(version));
        classNode.put("access", new ValueNode<>(access));
        classNode.put("name", new ValueNode<>(name));
        classNode.put("signature", new ValueNode<>(signature));
        classNode.put("super", new ValueNode<>(superName));

        ListNode interfacesNode = new LinkedListNode();
        for (String iface : interfaces) {
            interfacesNode.add(new ValueNode<>(iface));
        }
        classNode.put("interfaces", interfacesNode);

        classNode.put("fields", fields);
        classNode.put("methods", methods);
        classNode.put("recordComponents", recordComponents);

        classNode.put("nestMembers", nestMembers);
        classNode.put("permittedSubclasses", permittedSubclasses);
        classNode.put("innerClasses", innerClasses);

        classNode.put("annotations", annotations);
        classNode.put("attributes", attributes);
    }

    @Override
    public void visitSource(String source, String debug) {
        // Don't care
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        MapNode moduleNode = new LinkedHashMapNode();
        moduleNode.put("name", new ValueNode<>(name));
        moduleNode.put("access", new ValueNode<>(access));
        moduleNode.put("version", new ValueNode<>(version));
        classNode.put("module", moduleNode);

        return new ChasmModuleVisitor(api, moduleNode);
    }

    @Override
    public void visitNestHost(String nestHost) {
        classNode.put("nestHost", new ValueNode<>(nestHost));
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        classNode.put("ownerClass", new ValueNode<>(owner));
        classNode.put("ownerMethod", new ValueNode<>(name));
        classNode.put("ownerDescriptor", new ValueNode<>(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("typeRef", new ValueNode<>(typeRef));
        annotation.put("typePath", new ValueNode<>(typePath.toString()));
        annotation.put("values", values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new ValueNode<>(attribute));
    }

    @Override
    public void visitNestMember(String nestMember) {
        nestMembers.add(new ValueNode<>(nestMember));
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        permittedSubclasses.add(new ValueNode<>(permittedSubclass));
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        MapNode innerClass = new LinkedHashMapNode();
        innerClass.put("name", new ValueNode<>(name));
        innerClass.put("outerName", new ValueNode<>(outerName));
        innerClass.put("innerName", new ValueNode<>(innerName));
        innerClass.put("access", new ValueNode<>(access));
        innerClasses.add(innerClass);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        MapNode recordComponentNode = new LinkedHashMapNode();
        recordComponentNode.put("name", new ValueNode<>(name));
        recordComponentNode.put("descriptor", new ValueNode<>(descriptor));
        recordComponentNode.put("signature", new ValueNode<>(signature));
        recordComponents.add(recordComponentNode);

        return new ChasmRecordComponentVisitor(api, recordComponentNode);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        MapNode fieldNode = new LinkedHashMapNode();

        fieldNode.put("access", new ValueNode<>(access));
        fieldNode.put("name", new ValueNode<>(name));
        fieldNode.put("descriptor", new ValueNode<>(descriptor));
        fieldNode.put("signature", new ValueNode<>(signature));
        fieldNode.put("value", new ValueNode<>(value));

        return new ChasmFieldVisitor(api, fieldNode);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MapNode methodNode = new LinkedHashMapNode();

        methodNode.put("access", new ValueNode<>(access));
        methodNode.put("name", new ValueNode<>(name));
        methodNode.put("descriptor", new ValueNode<>(descriptor));
        methodNode.put("signature", new ValueNode<>(signature));

        ListNode exceptionsNode = new LinkedListNode();
        if (exceptions != null) {
            for (String exception : exceptions) {
                exceptionsNode.add(new ValueNode<>(exception));
            }
        }
        methodNode.put("exceptions", exceptionsNode);
        methods.add(methodNode);

        return new ChasmMethodVisitor(api, methodNode);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
