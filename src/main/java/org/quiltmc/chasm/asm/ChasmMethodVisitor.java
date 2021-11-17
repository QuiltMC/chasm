package org.quiltmc.chasm.asm;

import org.objectweb.asm.*;
import org.quiltmc.chasm.tree.*;

public class ChasmMethodVisitor extends MethodVisitor {
    private final MapNode methodNode;

    private final ListNode parameters = new LinkedListNode();

    private final ListNode annotations = new LinkedListNode();
    private final ListNode parameterAnnotations = new LinkedListNode();
    private final ListNode attributes = new LinkedListNode();

    private final MapNode code = new LinkedHashMapNode();
    private final ListNode instructions = new LinkedListNode();
    private final ListNode locals = new LinkedListNode();
    private final ListNode tryCatchBlocks = new LinkedListNode();

    private ListNode nextLabels = null;

    public ChasmMethodVisitor(int api, MapNode methodNode) {
        super(api);

        this.methodNode = methodNode;

        methodNode.put("parameters", parameters);
        methodNode.put("annotations", annotations);
        methodNode.put("parameterAnnotations", parameterAnnotations);
        methodNode.put("attributes", attributes);
    }

    @Override
    public void visitParameter(String name, int access) {
        MapNode parameterNode = new LinkedHashMapNode();
        parameterNode.put("name", new ValueNode<>(name));
        parameterNode.put("access", new ValueNode<>(access));
        parameters.add(parameterNode);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        // Don't care, inferred from actual parameter annotation count
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("parameter", new ValueNode<>(parameter));
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", new ValueNode<>(values));
        parameterAnnotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        MapNode annotationDefault = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        methodNode.put("annotationDefault", annotationDefault);
        methodNode.put("values", values);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", new ValueNode<>(values));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("typeRef", new ValueNode<>(typeRef));
        annotation.put("typePath", new ValueNode<>(typePath.toString()));
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", new ValueNode<>(values));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new ValueNode<>(attribute));
    }

    @Override
    public void visitCode() {
        methodNode.put("code", code);
        code.put("instructions", instructions);
        code.put("locals", locals);
        code.put("tryCatchBlocks", tryCatchBlocks);
    }

    private void visitInstruction(MapNode instructionNode) {
        if (nextLabels != null) {
            instructionNode.put("labels", nextLabels);
            nextLabels = null;
        }
        else {
            instructionNode.put("labels", new LinkedListNode());
        }

        instructionNode.put("annotations", new LinkedListNode());
    }

    @Override
    public void visitLabel(Label label) {
        if (nextLabels == null) {
            nextLabels = new LinkedListNode();
        }

        nextLabels.add(new ValueNode<>(label.toString()));
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        // Don't care, ClassReader is set to SKIP_FRAMES and ClassWriter to COMPUTE_FRAMES
    }

    @Override
    public void visitInsn(int opcode) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(opcode));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(opcode));
        instructionNode.put("operand", new ValueNode<>(operand));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(opcode));
        instructionNode.put("var", new ValueNode<>(var));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(opcode));
        instructionNode.put("type", new ValueNode<>(type));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(opcode));
        instructionNode.put("owner", new ValueNode<>(owner));
        instructionNode.put("name", new ValueNode<>(name));
        instructionNode.put("descriptor", new ValueNode<>(descriptor));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(opcode));
        instructionNode.put("owner", new ValueNode<>(owner));
        instructionNode.put("name", new ValueNode<>(name));
        instructionNode.put("descriptor", new ValueNode<>(descriptor));
        instructionNode.put("isInterface", new ValueNode<>(isInterface));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(Opcodes.INVOKEDYNAMIC));
        instructionNode.put("name", new ValueNode<>(name));
        instructionNode.put("descriptor", new ValueNode<>(descriptor));
        instructionNode.put("handle", getHandleNode(bootstrapMethodHandle));
        instructionNode.put("arguments", getArgumentsNode(bootstrapMethodArguments));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    private MapNode getHandleNode(Handle handle) {
        MapNode handleNode = new LinkedHashMapNode();
        handleNode.put("tag", new ValueNode<>(handle.getTag()));
        handleNode.put("owner", new ValueNode<>(handle.getOwner()));
        handleNode.put("name", new ValueNode<>(handle.getName()));
        handleNode.put("descriptor", new ValueNode<>(handle.getDesc()));
        handleNode.put("isInterface", new ValueNode<>(handle.isInterface()));
        return handleNode;
    }

    private ListNode getArgumentsNode(Object[] bootstrapMethodArguments) {
        ListNode argumentsNode = new LinkedListNode();
        for (Object arg : bootstrapMethodArguments) {
            if (arg instanceof Handle handle) {
                argumentsNode.add(getHandleNode(handle));
            }
            else if (arg instanceof ConstantDynamic constantDynamic) {
                MapNode constDynamicNode = new LinkedHashMapNode();
                constDynamicNode.put("name", new ValueNode<>(constantDynamic.getName()));
                constDynamicNode.put("descriptor", new ValueNode<>(constantDynamic.getDescriptor()));
                constDynamicNode.put("handle", getHandleNode(constantDynamic.getBootstrapMethod()));
                Object[] arguments = new Object[constantDynamic.getBootstrapMethodArgumentCount()];
                for (int i = 0; i < arguments.length; i++) {
                    arguments[i] = constantDynamic.getBootstrapMethodArgument(i);
                }
                constDynamicNode.put("arguments", getArgumentsNode(arguments));
                argumentsNode.add(constDynamicNode);
            }
            else {
                argumentsNode.add(new ValueNode<>(arg));
            }
        }
        return argumentsNode;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(opcode));
        instructionNode.put("target", new ValueNode<>(label.toString()));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitLdcInsn(Object value) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(Opcodes.LDC));
        instructionNode.put("value", new ValueNode<>(value));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(Opcodes.IINC));
        instructionNode.put("var", new ValueNode<>(var));
        instructionNode.put("increment", new ValueNode<>(increment));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(Opcodes.TABLESWITCH));
        instructionNode.put("default", new ValueNode<>(dflt.toString()));
        ListNode cases = new LinkedListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put("key", new ValueNode<>(min + i));
            caseNode.put("label", new ValueNode<>(labels[i].toString()));
            cases.add(caseNode);
        }
        instructionNode.put("cases", cases);
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(Opcodes.LOOKUPSWITCH));
        instructionNode.put("default", new ValueNode<>(dflt.toString()));
        ListNode cases = new LinkedListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put("key", new ValueNode<>(keys[i]));
            caseNode.put("label", new ValueNode<>(labels[i].toString()));
            cases.add(caseNode);
        }
        instructionNode.put("cases", cases);
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put("opcode", new ValueNode<>(Opcodes.MULTIANEWARRAY));
        instructionNode.put("descriptor", new ValueNode<>(descriptor));
        instructionNode.put("dimensions", new ValueNode<>(numDimensions));
        visitInstruction(instructionNode);
        instructions.add(instructionNode);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode instructionNode = (MapNode) instructions.get(instructions.size() - 1);

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", new ValueNode<>(values));
        annotation.put("typeRef", new ValueNode<>(typeRef));
        annotation.put("typePath", new ValueNode<>(typePath.toString()));

        ListNode annotations = (ListNode) instructionNode.get("annotations");
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        MapNode tryCatchBlock = new LinkedHashMapNode();
        tryCatchBlock.put("start", new ValueNode<>(start.toString()));
        tryCatchBlock.put("end", new ValueNode<>(end.toString()));
        tryCatchBlock.put("handler", new ValueNode<>(handler.toString()));
        tryCatchBlock.put("type", new ValueNode<>(type));
        tryCatchBlock.put("annotations", new LinkedListNode());
        tryCatchBlocks.add(tryCatchBlock);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode tryCatchBlock = (MapNode) tryCatchBlocks.get(tryCatchBlocks.size() - 1);

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new LinkedListNode();
        annotation.put("descriptor", new ValueNode<>(descriptor));
        annotation.put("visible", new ValueNode<>(visible));
        annotation.put("values", new ValueNode<>(values));
        annotation.put("typeRef", new ValueNode<>(typeRef));
        annotation.put("typePath", new ValueNode<>(typePath.toString()));

        ListNode annotations = (ListNode) tryCatchBlock.get("annotations");
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        // TODO: I think locals could be handled better than this
        MapNode localNode = new LinkedHashMapNode();
        localNode.put("name", new ValueNode<>(name));
        localNode.put("descriptor", new ValueNode<>(descriptor));
        localNode.put("signature", new ValueNode<>(signature));
        localNode.put("start", new ValueNode<>(start.toString()));
        localNode.put("end", new ValueNode<>(end.toString()));
        localNode.put("index", new ValueNode<>(index));
        locals.add(localNode);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor, boolean visible) {
        // TODO: How to handle this?
        return null;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        // Don't care
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // Don't care, ClassWriter is set to COMPUTE_MAXS
    }

    @Override
    public void visitEnd() {
        // TODO: Attach final labels to a NOP
        visitInsn(Opcodes.NOP);
    }
}
