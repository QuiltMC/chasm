package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.ClassVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class ClassNodeReader {
    private final MapNode classNode;

    public ClassNodeReader(MapNode classNode) {
        this.classNode = classNode;
    }

    public void accept(ClassVisitor visitor) {
        // visit
        int version = NodeUtils.getAsInt(classNode, NodeConstants.VERSION);
        int access = NodeUtils.getAsInt(classNode, NodeConstants.ACCESS);
        String name = NodeUtils.getAsString(classNode, NodeConstants.NAME);

        String signature = NodeUtils.getAsString(classNode, NodeConstants.SIGNATURE);
        String superClass = NodeUtils.getAsString(classNode, NodeConstants.SUPER);
        superClass = superClass == null ? "java/lang/Object" : superClass;

        ListNode interfacesNode = NodeUtils.getAsList(classNode, NodeConstants.INTERFACES);
        String[] interfaces = interfacesNode == null ? new String[0] :
                interfacesNode.getEntries().stream().map(NodeUtils::asString).toArray(String[]::new);

        visitor.visit(version, access, name, signature, superClass, interfaces);

        // visitSource
        visitSource(visitor);

        // visitModule
        if (classNode.getEntries().containsKey(NodeConstants.MODULE)) {
            ModuleNodeReader moduleWriter = new ModuleNodeReader(NodeUtils.getAsMap(classNode, NodeConstants.MODULE));
            moduleWriter.visitModule(visitor);
        }
        // visitNestHost
        visitNestHost(visitor);

        // visitOuterClass
        visitOuterClass(visitor);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(visitor);

        // visitNestMember
        visitNestMembers(visitor);

        //visitPermittedSubclass
        visitPermittedSubclasses(visitor);

        // visitInnerClass
        visitInnerClasses(visitor);

        // visitRecordComponent
        ListNode recordComponentListNode = NodeUtils.getAsList(classNode, NodeConstants.RECORD_COMPONENTS);
        if (recordComponentListNode != null) {
            for (Node node : recordComponentListNode.getEntries()) {
                MapNode componentNode = NodeUtils.asMap(node);
                RecordComponentNodeReader recordComponentNodeReader = new RecordComponentNodeReader(componentNode);
                recordComponentNodeReader.visitRecordComponent(visitor);
            }
        }

        // visitField
        ListNode fieldListNode = NodeUtils.getAsList(classNode, NodeConstants.FIELDS);
        if (fieldListNode != null) {
            for (Node node : fieldListNode.getEntries()) {
                MapNode fieldNode = NodeUtils.asMap(node);
                FieldNodeReader fieldNodeReader = new FieldNodeReader(fieldNode);
                fieldNodeReader.visitField(visitor);
            }
        }

        // visitMethod
        ListNode methodListNode = NodeUtils.getAsList(classNode, NodeConstants.METHODS);
        if (methodListNode != null) {
            for (Node node : methodListNode.getEntries()) {
                MapNode methodNode = NodeUtils.asMap(node);
                MethodNodeReader methodNodeReader = new MethodNodeReader(name, methodNode);
                methodNodeReader.visitMethod(visitor);
            }
        }

        // visitEnd
        visitor.visitEnd();
    }

    private void visitInnerClasses(ClassVisitor visitor) {
        ListNode innerClassesListNode = NodeUtils.getAsList(classNode, NodeConstants.INNER_CLASSES);
        if (innerClassesListNode == null) {
            return;
        }

        for (Node n : innerClassesListNode.getEntries()) {
            MapNode innerClass = NodeUtils.asMap(n);
            String name = NodeUtils.getAsString(innerClass, NodeConstants.NAME);
            String outerName = NodeUtils.getAsString(innerClass, NodeConstants.OUTER_NAME);
            String innerName = NodeUtils.getAsString(innerClass, NodeConstants.INNER_NAME);
            int access = NodeUtils.getAsInt(innerClass, NodeConstants.ACCESS);
            visitor.visitInnerClass(name, outerName, innerName, access);
        }
    }

    private void visitPermittedSubclasses(ClassVisitor visitor) {
        ListNode permittedSubclassesListNode = NodeUtils.getAsList(classNode, NodeConstants.PERMITTED_SUBCLASSES);
        if (permittedSubclassesListNode == null) {
            return;
        }

        for (Node n : permittedSubclassesListNode.getEntries()) {
            visitor.visitPermittedSubclass(NodeUtils.asString(n));
        }
    }

    private void visitNestMembers(ClassVisitor visitor) {
        ListNode nestMembersListNode = NodeUtils.getAsList(classNode, NodeConstants.NEST_MEMBERS);
        if (nestMembersListNode == null) {
            return;
        }

        for (Node n : nestMembersListNode.getEntries()) {
            visitor.visitNestMember(NodeUtils.asString(n));
        }
    }

    private void visitAnnotations(ClassVisitor visitor) {
        ListNode annotationsListNode = NodeUtils.getAsList(classNode, NodeConstants.ANNOTATIONS);
        if (annotationsListNode == null) {
            return;
        }

        for (Node annotation : annotationsListNode.getEntries()) {
            AnnotationNodeReader reader = new AnnotationNodeReader(annotation);
            reader.accept(visitor::visitAnnotation, visitor::visitTypeAnnotation);
        }
    }

    private void visitOuterClass(ClassVisitor visitor) {
        if (classNode.getEntries().containsKey(NodeConstants.OWNER_CLASS)) {
            String ownerClass = NodeUtils.getAsString(classNode, NodeConstants.OWNER_CLASS);
            String ownerMethod = NodeUtils.getAsString(classNode, NodeConstants.OWNER_METHOD);
            String ownerDescriptor = NodeUtils.getAsString(classNode, NodeConstants.OWNER_DESCRIPTOR);
            visitor.visitOuterClass(ownerClass, ownerMethod, ownerDescriptor);
        }
    }

    private void visitNestHost(ClassVisitor visitor) {
        if (classNode.getEntries().containsKey(NodeConstants.NEST_HOST)) {
            visitor.visitNestHost(NodeUtils.getAsString(classNode, NodeConstants.NEST_HOST));
        }
    }

    private void visitSource(ClassVisitor visitor) {
        String source = null;
        if (classNode.getEntries().containsKey(NodeConstants.SOURCE)) {
            source = NodeUtils.getAsString(classNode, NodeConstants.SOURCE);
        }

        String debug = null;
        if (classNode.getEntries().containsKey(NodeConstants.DEBUG)) {
            debug = NodeUtils.getAsString(classNode, NodeConstants.DEBUG);
        }

        visitor.visitSource(source, debug);
    }
}
