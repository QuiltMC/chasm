package org.quiltmc.chasm.internal.asm.visitor;

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
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;

public class ChasmClassVisitor extends ClassVisitor {
    private final Context context;

    private final MapNode classNode = Ast.emptyMap();
    private final ListNode fields = Ast.emptyList();
    private final ListNode methods = Ast.emptyList();
    private final ListNode recordComponents = Ast.emptyList();

    private final ListNode nestMembers = Ast.emptyList();
    private final ListNode permittedSubclasses = Ast.emptyList();
    private final ListNode innerClasses = Ast.emptyList();

    private final ListNode annotations = Ast.emptyList();

    public ChasmClassVisitor(Context context) {
        super(Opcodes.ASM9);
        this.context = context;
    }

    public MapNode getClassNode() {
        return classNode;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        // NOTE: Ensure parity with names in ClassNode
        classNode.put(NodeConstants.VERSION, Ast.literal(version));
        classNode.put(NodeConstants.ACCESS, Ast.literal(access));
        classNode.put(NodeConstants.NAME, Ast.literal(name));
        classNode.put(NodeConstants.SIGNATURE, Ast.nullableString(signature));
        classNode.put(NodeConstants.SUPER, Ast.nullableString(superName));

        if (interfaces != null) {
            classNode.put(NodeConstants.INTERFACES, Ast.list((Object[]) interfaces));
        }

        classNode.put(NodeConstants.FIELDS, fields);
        classNode.put(NodeConstants.METHODS, methods);
        classNode.put(NodeConstants.RECORD_COMPONENTS, recordComponents);

        classNode.put(NodeConstants.NEST_MEMBERS, nestMembers);
        classNode.put(NodeConstants.PERMITTED_SUBCLASSES, permittedSubclasses);
        classNode.put(NodeConstants.INNER_CLASSES, innerClasses);

        classNode.put(NodeConstants.ANNOTATIONS, annotations);
    }

    @Override
    public void visitSource(String source, String debug) {
        if (source != null) {
            classNode.put(NodeConstants.SOURCE, Ast.literal(source));
        }

        if (debug != null) {
            classNode.put(NodeConstants.DEBUG, Ast.literal(debug));
        }
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        MapNode moduleNode = Ast.map()
                .put(NodeConstants.NAME, name)
                .put(NodeConstants.ACCESS, access)
                .put(NodeConstants.VERSION, version)
                .build();
        classNode.put(NodeConstants.MODULE, moduleNode);

        return new ChasmModuleVisitor(api, moduleNode);
    }

    @Override
    public void visitNestHost(String nestHost) {
        classNode.put(NodeConstants.NEST_HOST, Ast.literal(nestHost));
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        classNode.put(NodeConstants.OWNER_CLASS, Ast.literal(owner));
        classNode.put(NodeConstants.OWNER_METHOD, Ast.nullableString(name));
        classNode.put(NodeConstants.OWNER_DESCRIPTOR, Ast.nullableString(descriptor));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode values = Ast.emptyMap();
        MapNode annotation = Ast.map()
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.VISIBLE, visible)
                .put(NodeConstants.VALUES, values)
                .build();
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode values = Ast.emptyMap();
        MapNode annotation = Ast.map()
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.VISIBLE, visible)
                .put(NodeConstants.TYPE_REF, typeRef)
                .put(NodeConstants.TYPE_PATH, typePath.toString())
                .put(NodeConstants.VALUES, values)
                .build();
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        throw new RuntimeException("Unknown attribute: " + attribute.type);
    }

    @Override
    public void visitNestMember(String nestMember) {
        nestMembers.add(Ast.literal(nestMember));
    }

    @Override
    public void visitPermittedSubclass(String permittedSubclass) {
        permittedSubclasses.add(Ast.literal(permittedSubclass));
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        MapNode innerClass = Ast.map()
                .put(NodeConstants.NAME, name)
                .put(NodeConstants.OUTER_NAME, outerName)
                .put(NodeConstants.INNER_NAME, innerName)
                .put(NodeConstants.ACCESS, access)
                .build();
        innerClasses.add(innerClass);
    }

    @Override
    public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
        MapNode recordComponentNode = Ast.map()
                .put(NodeConstants.NAME, name)
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.SIGNATURE, signature)
                .build();
        recordComponents.add(recordComponentNode);

        return new ChasmRecordComponentVisitor(api, recordComponentNode);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        MapNode fieldNode = Ast.map()
                .put(NodeConstants.ACCESS, access)
                .put(NodeConstants.NAME, name)
                .put(NodeConstants.DESCRIPTOR, descriptor)
                .put(NodeConstants.SIGNATURE, signature)
                .build();
        if (value != null) {
            fieldNode.put(NodeConstants.VALUE, NodeUtils.getValueNode(value));
        }

        fields.add(fieldNode);

        return new ChasmFieldVisitor(api, fieldNode);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
                                     String[] exceptions) {
        MapNode methodNode = Ast.emptyMap();
        methods.add(methodNode);

        String className = NodeUtils.getAsString(classNode, NodeConstants.NAME);
        String superName = NodeUtils.getAsString(classNode, NodeConstants.SUPER);
        int classAccess = NodeUtils.getAsInt(classNode, NodeConstants.ACCESS);
        List<Type> interfaces = NodeUtils.getAsList(classNode, NodeConstants.INTERFACES).getEntries().stream()
                .map(n -> Type.getObjectType(NodeUtils.asString(n))).collect(Collectors.toList());

        return new ChasmMethodVisitor(
                api,
                context,
                Type.getObjectType(className),
                superName == null ? null : Type.getObjectType(superName),
                interfaces,
                (classAccess & Opcodes.ACC_INTERFACE) != 0,
                methodNode, access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
