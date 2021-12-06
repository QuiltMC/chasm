package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.tree.LazyClassNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ClassNodeReader {
    private final MapNode classNode;

    public ClassNodeReader(MapNode classNode) {
        this.classNode = classNode;
    }

    public void accept(ClassVisitor visitor) {
        // Unmodified class
        if (classNode instanceof LazyClassNode) {
            ((LazyClassNode) classNode).getClassReader().accept(visitor, 0);
            return;
        }

        // visit
        int version = Node.asValue(classNode.get(NodeConstants.VERSION)).getValueAsInt();
        int access = Node.asValue(classNode.get(NodeConstants.ACCESS)).getValueAsInt();
        String name = Node.asValue(classNode.get(NodeConstants.NAME)).getValueAsString();

        ValueNode signatureNode = Node.asValue(classNode.get(NodeConstants.SIGNATURE));
        String signature = signatureNode == null ? null : signatureNode.getValueAsString();

        ValueNode superClassNode = Node.asValue(classNode.get(NodeConstants.SUPER));
        String superClass = superClassNode == null ? "java/lang/Object" : superClassNode.getValueAsString();

        ListNode interfacesNode = Node.asList(classNode.get(NodeConstants.INTERFACES));
        String[] interfaces = interfacesNode == null ? new String[0]
                :
                interfacesNode.stream().map(n -> Node.asValue(n).getValueAsString()).toArray(String[]::new);

        visitor.visit(version, access, name, signature, superClass, interfaces);

        // visitSource
        visitSource(visitor);

        // visitModule
        if (classNode.containsKey(NodeConstants.MODULE)) {
            ModuleNodeReader moduleWriter = new ModuleNodeReader(Node.asMap(classNode.get(NodeConstants.MODULE)));
            moduleWriter.visitModule(visitor);
        }
        // visitNestHost
        visitNestHost(visitor);

        // visitOuterClass
        visitOuterClass(visitor);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(visitor);

        // visitAttribute
        visitAttributes(visitor);

        // visitNestMember
        visitNestMembers(visitor);

        //visitPermittedSubclass
        visitPermittedSubclasses(visitor);

        // visitInnerClass
        visitInnerClasses(visitor);

        // visitRecordComponent
        ListNode recordComponentListNode = Node.asList(classNode.get(NodeConstants.RECORD_COMPONENTS));
        if (recordComponentListNode != null) {
            for (Node node : recordComponentListNode) {
                RecordComponentNodeReader recordComponentNodeReader = new RecordComponentNodeReader(Node.asMap(node));
                recordComponentNodeReader.visitRecordComponent(visitor);
            }
        }

        // visitField
        ListNode fieldListNode = Node.asList(classNode.get(NodeConstants.FIELDS));
        if (fieldListNode != null) {
            for (Node node : fieldListNode) {
                FieldNodeReader fieldNodeReader = new FieldNodeReader(Node.asMap(node));
                fieldNodeReader.visitField(visitor);
            }
        }

        // visitMethod
        ListNode methodListNode = Node.asList(classNode.get(NodeConstants.METHODS));
        if (methodListNode != null) {
            for (Node node : methodListNode) {
                MethodNodeReader methodNodeReader = new MethodNodeReader(Node.asMap(node));
                methodNodeReader.visitMethod(visitor);
            }
        }

        // visitEnd
        visitor.visitEnd();
    }

    private void visitInnerClasses(ClassVisitor visitor) {
        ListNode innerClassesListNode = Node.asList(classNode.get(NodeConstants.INNER_CLASSES));
        if (innerClassesListNode == null) {
            return;
        }
        for (Node n : innerClassesListNode) {
            MapNode innerClass = Node.asMap(n);
            ValueNode nameNode = Node.asValue(innerClass.get(NodeConstants.NAME));
            ValueNode outerNameNode = Node.asValue(innerClass.get(NodeConstants.OUTER_NAME));
            ValueNode innerNameNode = Node.asValue(innerClass.get(NodeConstants.INNER_NAME));
            ValueNode accessNode = Node.asValue(innerClass.get(NodeConstants.ACCESS));

            String name = nameNode.getValueAsString();
            String outerName = outerNameNode == null ? null : outerNameNode.getValueAsString();
            String innerName = innerNameNode == null ? null : innerNameNode.getValueAsString();
            int access = accessNode.getValueAsInt();

            visitor.visitInnerClass(name, outerName, innerName, access);
        }
    }

    private void visitPermittedSubclasses(ClassVisitor visitor) {
        ListNode permittedSubclassesListNode = Node.asList(classNode.get(NodeConstants.PERMITTED_SUBCLASSES));
        if (permittedSubclassesListNode == null) {
            return;
        }
        for (Node n : permittedSubclassesListNode) {
            visitor.visitPermittedSubclass(Node.asValue(n).getValueAsString());
        }
    }

    private void visitNestMembers(ClassVisitor visitor) {
        ListNode nestMembersListNode = Node.asList(classNode.get(NodeConstants.NEST_MEMBERS));
        if (nestMembersListNode == null) {
            return;
        }
        for (Node n : nestMembersListNode) {
            visitor.visitNestMember(Node.asValue(n).getValueAsString());
        }
    }

    private void visitAttributes(ClassVisitor visitor) {
        ListNode attributesListNode = Node.asList(classNode.get(NodeConstants.ATTRIBUTES));
        if (attributesListNode == null) {
            return;
        }
        for (Node n : attributesListNode) {
            visitor.visitAttribute(Node.asValue(n).getValueAs(Attribute.class));
        }
    }

    private void visitAnnotations(ClassVisitor visitor) {
        ListNode annotationsListNode = Node.asList(classNode.get(NodeConstants.ANNOTATIONS));
        if (annotationsListNode == null) {
            return;
        }
        for (Node n : annotationsListNode) {
            AnnotationNodeReader writer = new AnnotationNodeReader(n);
            writer.visitAnnotation(visitor::visitAnnotation, visitor::visitTypeAnnotation);
        }
    }

    private void visitOuterClass(ClassVisitor visitor) {
        if (classNode.containsKey(NodeConstants.OWNER_CLASS)) {
            String ownerClass = Node.asValue(classNode.get(NodeConstants.OWNER_CLASS)).getValueAsString();
            String ownerMethod = Node.asValue(classNode.get(NodeConstants.OWNER_METHOD)).getValueAsString();
            String ownerDescriptor = Node.asValue(classNode.get(NodeConstants.OWNER_DESCRIPTOR)).getValueAsString();
            visitor.visitOuterClass(ownerClass, ownerMethod, ownerDescriptor);
        }
    }

    private void visitNestHost(ClassVisitor visitor) {
        if (classNode.containsKey(NodeConstants.NEST_HOST)) {
            visitor.visitNestHost(Node.asValue(classNode.get(NodeConstants.NEST_HOST)).getValueAsString());
        }
    }

    private void visitSource(ClassVisitor visitor) {
        String source = null;
        if (classNode.containsKey(NodeConstants.SOURCE)) {
            source = Node.asValue(classNode.get(NodeConstants.SOURCE)).getValueAsString();
        }

        String debug = null;
        if (classNode.containsKey(NodeConstants.DEBUG)) {
            debug = Node.asValue(classNode.get(NodeConstants.DEBUG)).getValueAsString();
        }

        visitor.visitSource(source, debug);
    }
}
