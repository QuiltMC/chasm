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
                String name = ((ValueNode<String>) constDynamicNode.get(NodeConstants.NAME)).getValue();
                String descriptor = ((ValueNode<String>) constDynamicNode.get(NodeConstants.DESCRIPTOR)).getValue();
                Handle handle = getHandle((MapNode) constDynamicNode.get(NodeConstants.HANDLE));
                Object[] args = getArguments((ListNode) constDynamicNode.get(NodeConstants.ARGS));
                arguments[i] = new ConstantDynamic(name, descriptor, handle, args);
            }
        }

        return arguments;
    }

    public static Handle getHandle(MapNode handleNode) {
        int tag = ((ValueNode<Integer>) handleNode.get(NodeConstants.TAG)).getValue();
        String owner = ((ValueNode<String>) handleNode.get(NodeConstants.OWNER)).getValue();
        String name = ((ValueNode<String>) handleNode.get(NodeConstants.NAME)).getValue();
        String descriptor = ((ValueNode<String>) handleNode.get(NodeConstants.DESCRIPTOR)).getValue();
        boolean isInterface = ((ValueNode<Boolean>) handleNode.get(NodeConstants.IS_INTERFACE)).getValue();

        return new Handle(tag, owner, name, descriptor, isInterface);
    }

    public void accept(ClassVisitor visitor) {
        // Unmodified class
        if (classNode instanceof LazyClassNode) {
            ((LazyClassNode) classNode).getClassReader().accept(visitor, 0);
        }

        // visit
        int version = ((ValueNode<Integer>) classNode.get(NodeConstants.VERSION)).getValue();
        int access = ((ValueNode<Integer>) classNode.get(NodeConstants.ACCESS)).getValue();
        String name = ((ValueNode<String>) classNode.get(NodeConstants.NAME)).getValue();
        String signature = ((ValueNode<String>) classNode.get(NodeConstants.SIGNATURE)).getValue();
        String superClass = ((ValueNode<String>) classNode.get(NodeConstants.SUPER)).getValue();
        String[] interfaces = ((ListNode) classNode.get(NodeConstants.INTERFACES))
                .stream().map(n -> ((ValueNode<String>) n).getValue()).toArray(String[]::new);

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
            visitor.visitPermittedSubclass(((ValueNode<String>) n).getValue());
        }
    }

    private void visitNestMembers(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.NEST_MEMBERS)) {
            visitor.visitNestMember(((ValueNode<String>) n).getValue());
        }
    }

    private void visitAttributes(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.ATTRIBUTES)) {
            visitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(ClassVisitor visitor) {
        for (Node n : (ListNode) classNode.get(NodeConstants.ANNOTATIONS)) {
            ChasmAnnotationWriter writer = new ChasmAnnotationWriter((MapNode) n);
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
