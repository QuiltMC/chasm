package org.quiltmc.chasm.internal.asm.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.api.util.ClassInfoProvider;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.BooleanNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;

public class ChasmClassVisitor extends ClassVisitor {
    private final ClassInfoProvider classInfoProvider;

    private final MapNode classNode = new MapNode(new LinkedHashMap<>());
    private final ListNode fields = new ListNode(new ArrayList<>());
    private final ListNode methods = new ListNode(new ArrayList<>());
    private final ListNode recordComponents = new ListNode(new ArrayList<>());

    private final ListNode nestMembers = new ListNode(new ArrayList<>());
    private final ListNode permittedSubclasses = new ListNode(new ArrayList<>());
    private final ListNode innerClasses = new ListNode(new ArrayList<>());

    private final ListNode annotations = new ListNode(new ArrayList<>());

    public ChasmClassVisitor(ClassInfoProvider classInfoProvider) {
        super(Opcodes.ASM9);
        this.classInfoProvider = classInfoProvider;
    }

    public MapNode getClassNode() {
        return classNode;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // NOTE: Ensure parity with names in ClassNode
        classNode.getEntries().put(NodeConstants.VERSION, new IntegerNode(version));
        classNode.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        classNode.getEntries().put(NodeConstants.NAME, new StringNode(name));
        classNode.getEntries().put(NodeConstants.SIGNATURE, new StringNode(signature));
        classNode.getEntries().put(NodeConstants.SUPER, new StringNode(superName));

        ListNode interfacesNode = new ListNode(new ArrayList<>());
        for (String iface : interfaces) {
            interfacesNode.getEntries().add(new StringNode(iface));
        }
        classNode.getEntries().put(NodeConstants.INTERFACES, interfacesNode);

        classNode.getEntries().put(NodeConstants.FIELDS, fields);
        classNode.getEntries().put(NodeConstants.METHODS, methods);
        classNode.getEntries().put(NodeConstants.RECORD_COMPONENTS, recordComponents);

        classNode.getEntries().put(NodeConstants.NEST_MEMBERS, nestMembers);
        classNode.getEntries().put(NodeConstants.PERMITTED_SUBCLASSES, permittedSubclasses);
        classNode.getEntries().put(NodeConstants.INNER_CLASSES, innerClasses);

        classNode.getEntries().put(NodeConstants.ANNOTATIONS, annotations);
    }

    @Override
    public void visitSource(String source, String debug) {
        if (source != null) {
            classNode.getEntries().put(NodeConstants.SOURCE, new StringNode(source));
        }

        if (debug != null) {
            classNode.getEntries().put(NodeConstants.DEBUG, new StringNode(debug));
        }
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        MapNode moduleNode = new MapNode(new LinkedHashMap<>());
        moduleNode.getEntries().put(NodeConstants.NAME, new StringNode(name));
        moduleNode.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        moduleNode.getEntries().put(NodeConstants.VERSION, new StringNode(version));
        classNode.getEntries().put(NodeConstants.MODULE, moduleNode);

        return new ChasmModuleVisitor(api, moduleNode);
    }

    @Override
    public void visitNestHost(String nestHost) {
        classNode.getEntries().put(NodeConstants.NEST_HOST, new StringNode(nestHost));
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        classNode.getEntries().put(NodeConstants.OWNER_CLASS, new StringNode(owner));
        classNode.getEntries().put(NodeConstants.OWNER_METHOD, new StringNode(name));
        classNode.getEntries().put(NodeConstants.OWNER_DESCRIPTOR, new StringNode(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.TYPE_REF, new IntegerNode(typeRef));
        annotation.getEntries().put(NodeConstants.TYPE_PATH, new StringNode(typePath.toString()));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        throw new RuntimeException("Unknown attribute: " + attribute.type);
    }

    @Override
    public void visitNestMember(String nestMember) {
        nestMembers.getEntries().add(new StringNode(nestMember));
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        permittedSubclasses.getEntries().add(new StringNode(permittedSubclass));
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        MapNode innerClass = new MapNode(new LinkedHashMap<>());
        innerClass.getEntries().put(NodeConstants.NAME, new StringNode(name));
        innerClass.getEntries().put(NodeConstants.OUTER_NAME, new StringNode(outerName));
        innerClass.getEntries().put(NodeConstants.INNER_NAME, new StringNode(innerName));
        innerClass.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        innerClasses.getEntries().add(innerClass);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        MapNode recordComponentNode = new MapNode(new LinkedHashMap<>());
        recordComponentNode.getEntries().put(NodeConstants.NAME, new StringNode(name));
        recordComponentNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        recordComponentNode.getEntries().put(NodeConstants.SIGNATURE, new StringNode(signature));
        recordComponents.getEntries().add(recordComponentNode);

        return new ChasmRecordComponentVisitor(api, recordComponentNode);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        MapNode fieldNode = new MapNode(new LinkedHashMap<>());

        fieldNode.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        fieldNode.getEntries().put(NodeConstants.NAME, new StringNode(name));
        fieldNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        fieldNode.getEntries().put(NodeConstants.SIGNATURE, new StringNode(signature));
        if (value != null) {
            fieldNode.getEntries().put(NodeConstants.VALUE, NodeUtils.getValueNode(value));
        }
        fields.getEntries().add(fieldNode);

        return new ChasmFieldVisitor(api, fieldNode);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        MapNode methodNode = new MapNode(new LinkedHashMap<>());
        methods.getEntries().add(methodNode);

        StringNode nameNode = (StringNode) classNode.getEntries().get(NodeConstants.NAME);
        StringNode superNode = (StringNode) classNode.getEntries().get(NodeConstants.SUPER);
        IntegerNode accessNode = (IntegerNode) classNode.getEntries().get(NodeConstants.ACCESS);
        List<Type> interfaces = ((ListNode) classNode.getEntries().get(NodeConstants.INTERFACES)).getEntries().stream()
                .map(n -> Type.getObjectType((((StringNode) n).getValue()))).collect(Collectors.toList());

        return new ChasmMethodVisitor(
                api,
                classInfoProvider,
                Type.getObjectType(nameNode.getValue()),
                superNode == null ? null : Type.getObjectType(superNode.getValue()),
                interfaces,
                (accessNode.getValue() & Opcodes.ACC_INTERFACE) != 0,
                methodNode, access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
