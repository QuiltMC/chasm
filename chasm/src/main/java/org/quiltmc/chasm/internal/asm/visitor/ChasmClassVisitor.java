package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmClassVisitor extends ClassVisitor {
    private final MapNode classNode = new LinkedHashMapNode();
    private final ListNode fields = new ArrayListNode();
    private final ListNode methods = new ArrayListNode();
    private final ListNode recordComponents = new ArrayListNode();

    private final ListNode nestMembers = new ArrayListNode();
    private final ListNode permittedSubclasses = new ArrayListNode();
    private final ListNode innerClasses = new ArrayListNode();

    private final ListNode annotations = new ArrayListNode();
    private final ListNode attributes = new ArrayListNode();

    public ChasmClassVisitor() {
        super(Opcodes.ASM9);
    }

    public MapNode getClassNode() {
        return classNode;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // NOTE: Ensure parity with names in ClassNode
        classNode.put(NodeConstants.VERSION, new WrapperValueNode(version));
        classNode.put(NodeConstants.ACCESS, new WrapperValueNode(access));
        classNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        classNode.put(NodeConstants.SIGNATURE, new WrapperValueNode(signature));
        classNode.put(NodeConstants.SUPER, new WrapperValueNode(superName));

        ListNode interfacesNode = new ArrayListNode();
        for (String iface : interfaces) {
            interfacesNode.add(new WrapperValueNode(iface));
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
        if (source != null) {
            classNode.put(NodeConstants.SOURCE, new WrapperValueNode(source));
        }

        if (debug != null) {
            classNode.put(NodeConstants.DEBUG, new WrapperValueNode(debug));
        }
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        MapNode moduleNode = new LinkedHashMapNode();
        moduleNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        moduleNode.put(NodeConstants.ACCESS, new WrapperValueNode(access));
        moduleNode.put(NodeConstants.VERSION, new WrapperValueNode(version));
        classNode.put(NodeConstants.MODULE, moduleNode);

        return new ChasmModuleVisitor(api, moduleNode);
    }

    @Override
    public void visitNestHost(String nestHost) {
        classNode.put(NodeConstants.NEST_HOST, new WrapperValueNode(nestHost));
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        classNode.put(NodeConstants.OWNER_CLASS, new WrapperValueNode(owner));
        classNode.put(NodeConstants.OWNER_METHOD, new WrapperValueNode(name));
        classNode.put(NodeConstants.OWNER_DESCRIPTOR, new WrapperValueNode(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.TYPE_REF, new WrapperValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new WrapperValueNode(typePath.toString()));
        annotation.put(NodeConstants.VALUES, values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new WrapperValueNode(attribute));
    }

    @Override
    public void visitNestMember(String nestMember) {
        nestMembers.add(new WrapperValueNode(nestMember));
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        permittedSubclasses.add(new WrapperValueNode(permittedSubclass));
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        MapNode innerClass = new LinkedHashMapNode();
        innerClass.put(NodeConstants.NAME, new WrapperValueNode(name));
        innerClass.put(NodeConstants.OUTER_NAME, new WrapperValueNode(outerName));
        innerClass.put(NodeConstants.INNER_NAME, new WrapperValueNode(innerName));
        innerClass.put(NodeConstants.ACCESS, new WrapperValueNode(access));
        innerClasses.add(innerClass);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        MapNode recordComponentNode = new LinkedHashMapNode();
        recordComponentNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        recordComponentNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        recordComponentNode.put(NodeConstants.SIGNATURE, new WrapperValueNode(signature));
        recordComponents.add(recordComponentNode);

        return new ChasmRecordComponentVisitor(api, recordComponentNode);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        MapNode fieldNode = new LinkedHashMapNode();

        fieldNode.put(NodeConstants.ACCESS, new WrapperValueNode(access));
        fieldNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        fieldNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        fieldNode.put(NodeConstants.SIGNATURE, new WrapperValueNode(signature));
        fieldNode.put(NodeConstants.VALUE, new WrapperValueNode(value));
        fields.add(fieldNode);

        return new ChasmFieldVisitor(api, fieldNode);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        MapNode methodNode = new LinkedHashMapNode();
        methods.add(methodNode);

        return new ChasmMethodVisitor(api, methodNode, access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
