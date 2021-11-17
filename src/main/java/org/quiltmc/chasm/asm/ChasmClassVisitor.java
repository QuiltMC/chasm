package org.quiltmc.chasm.asm;

import org.objectweb.asm.*;
import org.quiltmc.chasm.NodeConstants;
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
        classNode.put(NodeConstants.VERSION, new ValueNode<>(version));
        classNode.put(NodeConstants.ACCESS, new ValueNode<>(access));
        classNode.put(NodeConstants.NAME, new ValueNode<>(name));
        classNode.put(NodeConstants.SIGNATURE, new ValueNode<>(signature));
        classNode.put(NodeConstants.SUPER, new ValueNode<>(superName));

        ListNode interfacesNode = new LinkedListNode();
        for (String iface : interfaces) {
            interfacesNode.add(new ValueNode<>(iface));
        }
        classNode.put(NodeConstants.INTERFACES, interfacesNode);

        classNode.put(NodeConstants.FIELDS, fields);
        classNode.put(NodeConstants.METHODS, methods);
        classNode.put(NodeConstants.RECORD_COMPONENTS, recordComponents);

        classNode.put(NodeConstants.NEST_MEMBERS, nestMembers);
        classNode.put(NodeConstants.PERMITTED_SUBCLASSES, permittedSubclasses);
        classNode.put(NodeConstants.INNER_CLASSES, innerClasses);

        classNode.put(NodeConstants.ANNOTATIONS, annotations);
        classNode.put(NodeConstants.ATTRIBUTES, attributes);
    }

    @Override
    public void visitSource(String source, String debug) {
        // Don't care
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        MapNode moduleNode = new LinkedHashMapNode();
        moduleNode.put(NodeConstants.NAME, new ValueNode<>(name));
        moduleNode.put(NodeConstants.ACCESS, new ValueNode<>(access));
        moduleNode.put(NodeConstants.VERSION, new ValueNode<>(version));
        classNode.put(NodeConstants.MODULE, moduleNode);

        return new ChasmModuleVisitor(api, moduleNode);
    }

    @Override
    public void visitNestHost(String nestHost) {
        classNode.put(NodeConstants.NEST_HOST, new ValueNode<>(nestHost));
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        classNode.put(NodeConstants.OWNER_CLASS, new ValueNode<>(owner));
        classNode.put(NodeConstants.OWNER_METHOD, new ValueNode<>(name));
        classNode.put(NodeConstants.OWNER_DESCRIPTOR, new ValueNode<>(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode<>(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode<>(visible));
        annotation.put(NodeConstants.VALUES, values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode<>(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode<>(visible));
        annotation.put(NodeConstants.TYPE_REF, new ValueNode<>(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new ValueNode<>(typePath.toString()));
        annotation.put(NodeConstants.VALUES, values);
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
        innerClass.put(NodeConstants.NAME, new ValueNode<>(name));
        innerClass.put(NodeConstants.OUTER_NAME, new ValueNode<>(outerName));
        innerClass.put(NodeConstants.INNER_NAME, new ValueNode<>(innerName));
        innerClass.put(NodeConstants.ACCESS, new ValueNode<>(access));
        innerClasses.add(innerClass);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        MapNode recordComponentNode = new LinkedHashMapNode();
        recordComponentNode.put(NodeConstants.NAME, new ValueNode<>(name));
        recordComponentNode.put(NodeConstants.DESCRIPTOR, new ValueNode<>(descriptor));
        recordComponentNode.put(NodeConstants.SIGNATURE, new ValueNode<>(signature));
        recordComponents.add(recordComponentNode);

        return new ChasmRecordComponentVisitor(api, recordComponentNode);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        MapNode fieldNode = new LinkedHashMapNode();

        fieldNode.put(NodeConstants.ACCESS, new ValueNode<>(access));
        fieldNode.put(NodeConstants.NAME, new ValueNode<>(name));
        fieldNode.put(NodeConstants.DESCRIPTOR, new ValueNode<>(descriptor));
        fieldNode.put(NodeConstants.SIGNATURE, new ValueNode<>(signature));
        fieldNode.put(NodeConstants.VALUE, new ValueNode<>(value));

        return new ChasmFieldVisitor(api, fieldNode);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MapNode methodNode = new LinkedHashMapNode();

        methodNode.put(NodeConstants.ACCESS, new ValueNode<>(access));
        methodNode.put(NodeConstants.NAME, new ValueNode<>(name));
        methodNode.put(NodeConstants.DESCRIPTOR, new ValueNode<>(descriptor));
        methodNode.put(NodeConstants.SIGNATURE, new ValueNode<>(signature));

        ListNode exceptionsNode = new LinkedListNode();
        if (exceptions != null) {
            for (String exception : exceptions) {
                exceptionsNode.add(new ValueNode<>(exception));
            }
        }
        methodNode.put(NodeConstants.EXCEPTIONS, exceptionsNode);
        methods.add(methodNode);

        return new ChasmMethodVisitor(api, methodNode);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
