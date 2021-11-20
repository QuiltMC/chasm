package org.quiltmc.chasm.asm.writer;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.quiltmc.chasm.LazyClassNode;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

@SuppressWarnings("unchecked")
public class ChasmClassWriter {
    private final MapNode classNode;

    public ChasmClassWriter(MapNode classNode) {
        this.classNode = classNode;
    }

    public static Object[] getArguments(ListNode argumentNode) {
        Object[] arguments = new Object[argumentNode.size()];
        for (int i = 0; i < arguments.length; i++) {
            Node argNode = argumentNode.get(i);
            if (argNode instanceof ValueNode<?> valueNode) {
                arguments[i] = valueNode.getValue();
            } else if (((MapNode) argNode).containsKey(NodeConstants.TAG)) {
                arguments[i] = getHandle((MapNode) argNode);
            } else {
                MapNode constDynamicNode = (MapNode) argNode;
                String name = constDynamicNode.get(NodeConstants.NAME).getAsString();
                String descriptor = constDynamicNode.get(NodeConstants.DESCRIPTOR).getAsString();
                Handle handle = getHandle((MapNode) constDynamicNode.get(NodeConstants.HANDLE));
                Object[] args = getArguments((ListNode) constDynamicNode.get(NodeConstants.ARGS));
                arguments[i] = new ConstantDynamic(name, descriptor, handle, args);
            }
        }

        return arguments;
    }

    public static Handle getHandle(MapNode handleNode) {
        int tag = handleNode.get(NodeConstants.TAG).getAsInt();
        String owner = handleNode.get(NodeConstants.OWNER).getAsString();
        String name = handleNode.get(NodeConstants.NAME).getAsString();
        String descriptor = handleNode.get(NodeConstants.DESCRIPTOR).getAsString();
        boolean isInterface = handleNode.get(NodeConstants.IS_INTERFACE).getAsBoolean();

        return new Handle(tag, owner, name, descriptor, isInterface);
    }

    public void accept(ClassVisitor visitor) {
        // Unmodified class
        if (classNode instanceof LazyClassNode) {
            ((LazyClassNode) classNode).getClassReader().accept(visitor, 0);
            return;
        }

        // visit
        int version = classNode.get(NodeConstants.VERSION).getAsInt();
        int access = classNode.get(NodeConstants.ACCESS).getAsInt();
        String name = classNode.get(NodeConstants.NAME).getAsString();
        String signature = classNode.get(NodeConstants.SIGNATURE).getAsString();
        String superClass = classNode.get(NodeConstants.SUPER).getAsString();
        String[] interfaces = ((ListNode) classNode.get(NodeConstants.INTERFACES))
                .stream().map(n -> n.getAsString()).toArray(String[]::new);

        visitor.visit(version, access, name, signature, superClass, interfaces);

        // visitSource
        visitSource(visitor);

        // visitModule
        if (classNode.containsKey(NodeConstants.MODULE)) {
            ChasmModuleWriter moduleWriter = new ChasmModuleWriter((MapNode) classNode.get(NodeConstants.MODULE));
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
        for (Node node : (ListNode) classNode.get(NodeConstants.RECORD_COMPONENTS)) {
            ChasmRecordComponentWriter chasmRecordComponentWriter = new ChasmRecordComponentWriter((MapNode) node);
            chasmRecordComponentWriter.visitRecordComponent(visitor);
        }

        // visitField
        for (Node node : (ListNode) classNode.get(NodeConstants.FIELDS)) {
            ChasmFieldWriter chasmFieldWriter = new ChasmFieldWriter((MapNode) node);
            chasmFieldWriter.visitField(visitor);
        }

        // visitMethod
        for (Node node : (ListNode) classNode.get(NodeConstants.METHODS)) {
            ChasmMethodWriter chasmMethodWriter = new ChasmMethodWriter((MapNode) node);
            chasmMethodWriter.visitMethod(visitor);
        }

        // visitEnd
        visitor.visitEnd();
    }

    private void visitInnerClasses(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.INNER_CLASSES)) {
            MapNode innerClass = (MapNode) n;
            ValueNode<String> name = (ValueNode<String>) innerClass.get(NodeConstants.NAME);
            ValueNode<String> outerName = (ValueNode<String>) innerClass.get(NodeConstants.OUTER_NAME);
            ValueNode<String> innerName = (ValueNode<String>) innerClass.get(NodeConstants.INNER_NAME);
            ValueNode<Integer> access = (ValueNode<Integer>) innerClass.get(NodeConstants.ACCESS);

            visitor.visitInnerClass(name.getValue(), outerName.getValue(), innerName.getValue(), access.getValue());
        }
    }

    private void visitPermittedSubclasses(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.PERMITTED_SUBCLASSES)) {
            visitor.visitPermittedSubclass(n.getAsString());
        }
    }

    private void visitNestMembers(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.NEST_MEMBERS)) {
            visitor.visitNestMember(n.getAsString());
        }
    }

    private void visitAttributes(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.ATTRIBUTES)) {
            visitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.ANNOTATIONS)) {
            ChasmAnnotationWriter writer = new ChasmAnnotationWriter(n);
            writer.visitAnnotation(visitor::visitAnnotation, visitor::visitTypeAnnotation);
        }
    }

    private void visitOuterClass(ClassVisitor visitor) {
        if (classNode.containsKey(NodeConstants.OWNER_CLASS)) {
            String ownerClass = classNode.get(NodeConstants.OWNER_CLASS).getAsString();
            String ownerMethod = classNode.get(NodeConstants.OWNER_METHOD).getAsString();
            String ownerDescriptor = classNode.get(NodeConstants.OWNER_DESCRIPTOR).getAsString();
            visitor.visitOuterClass(ownerClass, ownerMethod, ownerDescriptor);
        }
    }

    private void visitNestHost(ClassVisitor visitor) {
        if (classNode.containsKey(NodeConstants.NEST_HOST)) {
            visitor.visitNestHost(classNode.get(NodeConstants.NEST_HOST).getAsString());
        }
    }

    private void visitSource(ClassVisitor visitor) {
        String source = null;
        if (classNode.containsKey(NodeConstants.SOURCE)) {
            source = classNode.get(NodeConstants.SOURCE).getAsString();
        }

        String debug = null;
        if (classNode.containsKey(NodeConstants.DEBUG)) {
            debug = classNode.get(NodeConstants.DEBUG).getAsString();
        }

        visitor.visitSource(source, debug);
    }
}
