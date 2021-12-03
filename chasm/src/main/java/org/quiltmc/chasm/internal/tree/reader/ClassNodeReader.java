package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.LazyClassNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

@SuppressWarnings("unchecked")
public class ClassNodeReader {
    private final MapNode<Node> classNode;

    public ClassNodeReader(MapNode<Node> classNode) {
        this.classNode = classNode;
    }

    public void accept(ClassVisitor visitor) {
        // Unmodified class
        if (classNode instanceof LazyClassNode) {
            ((LazyClassNode) classNode).getClassReader().accept(visitor, 0);
            return;
        }

        // visit
        int version = ((ValueNode<Integer>) classNode.get(NodeConstants.VERSION)).getValue();
        int access = ((ValueNode<Integer>) classNode.get(NodeConstants.ACCESS)).getValue();
        String name = ((ValueNode<String>) classNode.get(NodeConstants.NAME)).getValue();

        ValueNode<String> signatureNode = (ValueNode<String>) classNode.get(NodeConstants.SIGNATURE);
        String signature = signatureNode == null ? null : signatureNode.getValue();

        ValueNode<String> superClassNode = (ValueNode<String>) classNode.get(NodeConstants.SUPER);
        String superClass = superClassNode == null ? "java/lang/Object" : superClassNode.getValue();

        ListNode<Node> interfacesNode = (ListNode<Node>) classNode.get(NodeConstants.INTERFACES);
        String[] interfaces = interfacesNode == null ? new String[0]
                :
                interfacesNode.stream().map(n -> ((ValueNode<String>) n).getValue()).toArray(String[]::new);

        visitor.visit(version, access, name, signature, superClass, interfaces);

        // visitSource
        visitSource(visitor);

        // visitModule
        if (classNode.containsKey(NodeConstants.MODULE)) {
            ModuleNodeReader moduleWriter = new ModuleNodeReader((MapNode<Node>) classNode.get(NodeConstants.MODULE));
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
        ListNode<Node> recordComponentListNode = (ListNode<Node>) classNode.get(NodeConstants.RECORD_COMPONENTS);
        if (recordComponentListNode != null) {
            for (Node node : recordComponentListNode) {
                RecordComponentNodeReader recordComponentNodeReader = new RecordComponentNodeReader(
                        (MapNode<Node>) node);
                recordComponentNodeReader.visitRecordComponent(visitor);
            }
        }

        // visitField
        ListNode<Node> fieldListNode = (ListNode<Node>) classNode.get(NodeConstants.FIELDS);
        if (fieldListNode != null) {
            for (Node node : fieldListNode) {
                FieldNodeReader fieldNodeReader = new FieldNodeReader((MapNode<Node>) node);
                fieldNodeReader.visitField(visitor);
            }
        }

        // visitMethod
        ListNode<Node> methodListNode = (ListNode<Node>) classNode.get(NodeConstants.METHODS);
        if (methodListNode != null) {
            for (Node node : methodListNode) {
                MethodNodeReader methodNodeReader = new MethodNodeReader((MapNode<Node>) node);
                methodNodeReader.visitMethod(visitor);
            }
        }

        // visitEnd
        visitor.visitEnd();
    }

    private void visitInnerClasses(ClassVisitor visitor) {
        ListNode<Node> innerClassesListNode = (ListNode<Node>) classNode.get(NodeConstants.INNER_CLASSES);
        if (innerClassesListNode == null) {
            return;
        }
        for (Node n : innerClassesListNode) {
            MapNode<Node> innerClass = (MapNode<Node>) n;
            ValueNode<String> nameNode = (ValueNode<String>) innerClass.get(NodeConstants.NAME);
            ValueNode<String> outerNameNode = (ValueNode<String>) innerClass.get(NodeConstants.OUTER_NAME);
            ValueNode<String> innerNameNode = (ValueNode<String>) innerClass.get(NodeConstants.INNER_NAME);
            ValueNode<Integer> accessNode = (ValueNode<Integer>) innerClass.get(NodeConstants.ACCESS);

            String name = nameNode.getValue();
            String outerName = outerNameNode == null ? null : outerNameNode.getValue();
            String innerName = innerNameNode == null ? null : innerNameNode.getValue();
            int access = accessNode.getValue();

            visitor.visitInnerClass(name, outerName, innerName, access);
        }
    }

    private void visitPermittedSubclasses(ClassVisitor visitor) {
        ListNode<Node> permittedSubclassesListNode = (ListNode<Node>) classNode.get(NodeConstants.PERMITTED_SUBCLASSES);
        if (permittedSubclassesListNode == null) {
            return;
        }
        for (Node n : permittedSubclassesListNode) {
            visitor.visitPermittedSubclass(((ValueNode<String>) n).getValue());
        }
    }

    private void visitNestMembers(ClassVisitor visitor) {
        ListNode<Node> nestMembersListNode = (ListNode<Node>) classNode.get(NodeConstants.NEST_MEMBERS);
        if (nestMembersListNode == null) {
            return;
        }
        for (Node n : nestMembersListNode) {
            visitor.visitNestMember(((ValueNode<String>) n).getValue());
        }
    }

    private void visitAttributes(ClassVisitor visitor) {
        ListNode<Node> attributesListNode = (ListNode<Node>) classNode.get(NodeConstants.ATTRIBUTES);
        if (attributesListNode == null) {
            return;
        }
        for (Node n : attributesListNode) {
            visitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(ClassVisitor visitor) {
        ListNode<Node> annotationsListNode = (ListNode<Node>) classNode.get(NodeConstants.ANNOTATIONS);
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
            String ownerClass = ((ValueNode<String>) classNode.get(NodeConstants.OWNER_CLASS)).getValue();
            String ownerMethod = ((ValueNode<String>) classNode.get(NodeConstants.OWNER_METHOD)).getValue();
            String ownerDescriptor = ((ValueNode<String>) classNode.get(NodeConstants.OWNER_DESCRIPTOR)).getValue();
            visitor.visitOuterClass(ownerClass, ownerMethod, ownerDescriptor);
        }
    }

    private void visitNestHost(ClassVisitor visitor) {
        if (classNode.containsKey(NodeConstants.NEST_HOST)) {
            visitor.visitNestHost(((ValueNode<String>) classNode.get(NodeConstants.NEST_HOST)).getValue());
        }
    }

    private void visitSource(ClassVisitor visitor) {
        String source = null;
        if (classNode.containsKey(NodeConstants.SOURCE)) {
            source = ((ValueNode<String>) classNode.get(NodeConstants.SOURCE)).getValue();
        }

        String debug = null;
        if (classNode.containsKey(NodeConstants.DEBUG)) {
            debug = ((ValueNode<String>) classNode.get(NodeConstants.DEBUG)).getValue();
        }

        visitor.visitSource(source, debug);
    }
}
