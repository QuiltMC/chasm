package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.LazyClassNode;
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
        int version = ((ValueNode) classNode.get(NodeConstants.VERSION)).getValueAsInt();
        int access = ((ValueNode) classNode.get(NodeConstants.ACCESS)).getValueAsInt();
        String name = ((ValueNode) classNode.get(NodeConstants.NAME)).getValueAsString();

        ValueNode signatureNode = (ValueNode) classNode.get(NodeConstants.SIGNATURE);
        String signature = signatureNode == null ? null : signatureNode.getValueAsString();

        ValueNode superClassNode = (ValueNode) classNode.get(NodeConstants.SUPER);
        String superClass = superClassNode == null ? "java/lang/Object" : superClassNode.getValueAsString();

        ListNode interfacesNode = (ListNode) classNode.get(NodeConstants.INTERFACES);
        String[] interfaces = interfacesNode == null ? new String[0]
                :
                interfacesNode.stream().map(n -> ((ValueNode) n).getValueAsString()).toArray(String[]::new);

        visitor.visit(version, access, name, signature, superClass, interfaces);

        // visitSource
        visitSource(visitor);

        // visitModule
        if (classNode.containsKey(NodeConstants.MODULE)) {
            ModuleNodeReader moduleWriter = new ModuleNodeReader((MapNode) classNode.get(NodeConstants.MODULE));
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
        ListNode recordComponentListNode = (ListNode) classNode.get(NodeConstants.RECORD_COMPONENTS);
        if (recordComponentListNode != null) {
            for (Node node : recordComponentListNode) {
                RecordComponentNodeReader recordComponentNodeReader = new RecordComponentNodeReader((MapNode) node);
                recordComponentNodeReader.visitRecordComponent(visitor);
            }
        }

        // visitField
        ListNode fieldListNode = (ListNode) classNode.get(NodeConstants.FIELDS);
        if (fieldListNode != null) {
            for (Node node : fieldListNode) {
                FieldNodeReader fieldNodeReader = new FieldNodeReader((MapNode) node);
                fieldNodeReader.visitField(visitor);
            }
        }

        // visitMethod
        ListNode methodListNode = (ListNode) classNode.get(NodeConstants.METHODS);
        if (methodListNode != null) {
            for (Node node : methodListNode) {
                MethodNodeReader methodNodeReader = new MethodNodeReader((MapNode) node);
                methodNodeReader.visitMethod(visitor);
            }
        }

        // visitEnd
        visitor.visitEnd();
    }

    private void visitInnerClasses(ClassVisitor visitor) {
        ListNode innerClassesListNode = (ListNode) classNode.get(NodeConstants.INNER_CLASSES);
        if (innerClassesListNode == null) {
            return;
        }
        for (Node n : innerClassesListNode) {
            MapNode innerClass = (MapNode) n;
            ValueNode nameNode = (ValueNode) innerClass.get(NodeConstants.NAME);
            ValueNode outerNameNode = (ValueNode) innerClass.get(NodeConstants.OUTER_NAME);
            ValueNode innerNameNode = (ValueNode) innerClass.get(NodeConstants.INNER_NAME);
            ValueNode accessNode = (ValueNode) innerClass.get(NodeConstants.ACCESS);

            String name = nameNode.getValueAsString();
            String outerName = outerNameNode == null ? null : outerNameNode.getValueAsString();
            String innerName = innerNameNode == null ? null : innerNameNode.getValueAsString();
            int access = accessNode.getValueAsInt();

            visitor.visitInnerClass(name, outerName, innerName, access);
        }
    }

    private void visitPermittedSubclasses(ClassVisitor visitor) {
        ListNode permittedSubclassesListNode = (ListNode) classNode.get(NodeConstants.PERMITTED_SUBCLASSES);
        if (permittedSubclassesListNode == null) {
            return;
        }
        for (Node n : permittedSubclassesListNode) {
            visitor.visitPermittedSubclass(((ValueNode) n).getValueAsString());
        }
    }

    private void visitNestMembers(ClassVisitor visitor) {
        ListNode nestMembersListNode = (ListNode) classNode.get(NodeConstants.NEST_MEMBERS);
        if (nestMembersListNode == null) {
            return;
        }
        for (Node n : nestMembersListNode) {
            visitor.visitNestMember(((ValueNode) n).getValueAsString());
        }
    }

    private void visitAttributes(ClassVisitor visitor) {
        ListNode attributesListNode = (ListNode) classNode.get(NodeConstants.ATTRIBUTES);
        if (attributesListNode == null) {
            return;
        }
        for (Node n : attributesListNode) {
            visitor.visitAttribute(((ValueNode) n).getValueAs(Attribute.class));
        }
    }

    private void visitAnnotations(ClassVisitor visitor) {
        ListNode annotationsListNode = (ListNode) classNode.get(NodeConstants.ANNOTATIONS);
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
            String ownerClass = ((ValueNode) classNode.get(NodeConstants.OWNER_CLASS)).getValueAsString();
            String ownerMethod = ((ValueNode) classNode.get(NodeConstants.OWNER_METHOD)).getValueAsString();
            String ownerDescriptor = ((ValueNode) classNode.get(NodeConstants.OWNER_DESCRIPTOR)).getValueAsString();
            visitor.visitOuterClass(ownerClass, ownerMethod, ownerDescriptor);
        }
    }

    private void visitNestHost(ClassVisitor visitor) {
        if (classNode.containsKey(NodeConstants.NEST_HOST)) {
            visitor.visitNestHost(((ValueNode) classNode.get(NodeConstants.NEST_HOST)).getValueAsString());
        }
    }

    private void visitSource(ClassVisitor visitor) {
        String source = null;
        if (classNode.containsKey(NodeConstants.SOURCE)) {
            source = ((ValueNode) classNode.get(NodeConstants.SOURCE)).getValueAsString();
        }

        String debug = null;
        if (classNode.containsKey(NodeConstants.DEBUG)) {
            debug = ((ValueNode) classNode.get(NodeConstants.DEBUG)).getValueAsString();
        }

        visitor.visitSource(source, debug);
    }
}
