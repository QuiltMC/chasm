package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmMethodVisitor extends MethodVisitor {
    private final MapNode methodNode;

    private final ListNode parameters = new ArrayListNode();

    private final ListNode annotations = new ArrayListNode();
    private final ListNode parameterAnnotations = new ArrayListNode();
    private final ListNode attributes = new ArrayListNode();

    private final MapNode code = new LinkedHashMapNode();
    private final ListNode instructions = new ArrayListNode();
    private final ListNode locals = new ArrayListNode();
    private final ListNode tryCatchBlocks = new ArrayListNode();

    private final ListNode lineNumbers = new ArrayListNode();

    public ChasmMethodVisitor(int api, MapNode methodNode) {
        super(api);

        this.methodNode = methodNode;

        methodNode.put(NodeConstants.PARAMETERS, parameters);
        methodNode.put(NodeConstants.ANNOTATIONS, annotations);
        methodNode.put(NodeConstants.PARAMETER_ANNOTATIONS, parameterAnnotations);
        methodNode.put(NodeConstants.ATTRIBUTES, attributes);
    }

    @Override
    public void visitParameter(String name, int access) {
        MapNode parameterNode = new LinkedHashMapNode();
        parameterNode.put(NodeConstants.NAME, new ValueNode(name));
        parameterNode.put(NodeConstants.ACCESS, new ValueNode(access));
        parameters.add(parameterNode);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        // Don't care, inferred from actual parameter annotation count
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.PARAMETER, new ValueNode(parameter));
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);
        parameterAnnotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        MapNode annotationDefault = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        methodNode.put(NodeConstants.ANNOTATION_DEFAULT, annotationDefault);
        methodNode.put(NodeConstants.VALUES, values);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.TYPE_REF, new ValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new ValueNode(typePath.toString()));
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, new ValueNode(values));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new ValueNode(attribute));
    }

    @Override
    public void visitCode() {
        methodNode.put(NodeConstants.CODE, code);
        code.put(NodeConstants.INSTRUCTIONS, instructions);
        code.put(NodeConstants.LOCALS, locals);
        code.put(NodeConstants.TRY_CATCH_BLOCKS, tryCatchBlocks);
        code.put(NodeConstants.LINE_NUMBERS, lineNumbers);
    }

    @Override
    public void visitLabel(Label label) {
        MapNode labelNode = new LinkedHashMapNode();
        labelNode.put(NodeConstants.LABEL, new ValueNode(label.toString()));
        instructions.add(labelNode);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        // Don't care, ClassReader is set to SKIP_FRAMES and ClassWriter to COMPUTE_FRAMES
    }

    @Override
    public void visitInsn(int opcode) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructions.add(instructionNode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.OPERAND, new ValueNode(operand));
        instructions.add(instructionNode);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.VAR, new ValueNode(var));
        instructions.add(instructionNode);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.TYPE, new ValueNode(type));
        instructions.add(instructionNode);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.OWNER, new ValueNode(owner));
        instructionNode.put(NodeConstants.NAME, new ValueNode(name));
        instructionNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        instructions.add(instructionNode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.OWNER, new ValueNode(owner));
        instructionNode.put(NodeConstants.NAME, new ValueNode(name));
        instructionNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        instructionNode.put(NodeConstants.IS_INTERFACE, new ValueNode(isInterface));
        instructions.add(instructionNode);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.INVOKEDYNAMIC));
        instructionNode.put(NodeConstants.NAME, new ValueNode(name));
        instructionNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        instructionNode.put(NodeConstants.HANDLE, getHandleNode(bootstrapMethodHandle));
        instructionNode.put(NodeConstants.ARGUMENTS, getArgumentsNode(bootstrapMethodArguments));
        instructions.add(instructionNode);
    }

    private MapNode getHandleNode(Handle handle) {
        MapNode handleNode = new LinkedHashMapNode();
        handleNode.put(NodeConstants.TAG, new ValueNode(handle.getTag()));
        handleNode.put(NodeConstants.OWNER, new ValueNode(handle.getOwner()));
        handleNode.put(NodeConstants.NAME, new ValueNode(handle.getName()));
        handleNode.put(NodeConstants.DESCRIPTOR, new ValueNode(handle.getDesc()));
        handleNode.put(NodeConstants.IS_INTERFACE, new ValueNode(handle.isInterface()));
        return handleNode;
    }

    private ListNode getArgumentsNode(Object[] bootstrapMethodArguments) {
        ListNode argumentsNode = new ArrayListNode();
        for (Object arg : bootstrapMethodArguments) {
            if (arg instanceof Handle) {
                argumentsNode.add(getHandleNode((Handle) arg));
            } else if (arg instanceof ConstantDynamic) {
                ConstantDynamic constantDynamic = (ConstantDynamic) arg;
                MapNode constDynamicNode = new LinkedHashMapNode();
                constDynamicNode.put(NodeConstants.NAME, new ValueNode(constantDynamic.getName()));
                constDynamicNode.put(NodeConstants.DESCRIPTOR, new ValueNode(constantDynamic.getDescriptor()));
                constDynamicNode.put(NodeConstants.HANDLE, getHandleNode(constantDynamic.getBootstrapMethod()));
                Object[] arguments = new Object[constantDynamic.getBootstrapMethodArgumentCount()];
                for (int i = 0; i < arguments.length; i++) {
                    arguments[i] = constantDynamic.getBootstrapMethodArgument(i);
                }
                constDynamicNode.put(NodeConstants.ARGUMENTS, getArgumentsNode(arguments));
                argumentsNode.add(constDynamicNode);
            } else {
                argumentsNode.add(new ValueNode(arg));
            }
        }
        return argumentsNode;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.TARGET, new ValueNode(label.toString()));
        instructions.add(instructionNode);
    }

    @Override
    public void visitLdcInsn(Object value) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.LDC));
        instructionNode.put(NodeConstants.VALUE, new ValueNode(value));
        instructions.add(instructionNode);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.IINC));
        instructionNode.put(NodeConstants.VAR, new ValueNode(var));
        instructionNode.put(NodeConstants.INCREMENT, new ValueNode(increment));
        instructions.add(instructionNode);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.TABLESWITCH));
        instructionNode.put(NodeConstants.DEFAULT, new ValueNode(dflt.toString()));
        ListNode cases = new ArrayListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put(NodeConstants.KEY, new ValueNode(min + i));
            caseNode.put(NodeConstants.LABEL, new ValueNode(labels[i].toString()));
            cases.add(caseNode);
        }
        instructionNode.put(NodeConstants.CASES, cases);
        instructions.add(instructionNode);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.LOOKUPSWITCH));
        instructionNode.put(NodeConstants.DEFAULT, new ValueNode(dflt.toString()));
        ListNode cases = new ArrayListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put(NodeConstants.KEY, new ValueNode(keys[i]));
            caseNode.put(NodeConstants.LABEL, new ValueNode(labels[i].toString()));
            cases.add(caseNode);
        }
        instructionNode.put(NodeConstants.CASES, cases);
        instructions.add(instructionNode);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.MULTIANEWARRAY));
        instructionNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        instructionNode.put(NodeConstants.DIMENSIONS, new ValueNode(numDimensions));
        instructions.add(instructionNode);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode instructionNode = (MapNode) instructions.get(instructions.size() - 1);

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, new ValueNode(values));
        annotation.put(NodeConstants.TYPE_REF, new ValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new ValueNode(typePath.toString()));

        ListNode annotations = (ListNode) instructionNode
                .computeIfAbsent(NodeConstants.ANNOTATIONS, s -> new ArrayListNode());
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        MapNode tryCatchBlock = new LinkedHashMapNode();
        tryCatchBlock.put(NodeConstants.START, new ValueNode(start.toString()));
        tryCatchBlock.put(NodeConstants.END, new ValueNode(end.toString()));
        tryCatchBlock.put(NodeConstants.HANDLER, new ValueNode(handler.toString()));
        tryCatchBlock.put(NodeConstants.TYPE, new ValueNode(type));
        tryCatchBlock.put(NodeConstants.ANNOTATIONS, new ArrayListNode());
        tryCatchBlocks.add(tryCatchBlock);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor,
                                                     boolean visible) {
        MapNode tryCatchBlock = (MapNode) tryCatchBlocks.get(tryCatchBlocks.size() - 1);

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, new ValueNode(values));
        annotation.put(NodeConstants.TYPE_REF, new ValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new ValueNode(typePath.toString()));

        ListNode annotations = (ListNode) tryCatchBlock.get(NodeConstants.ANNOTATIONS);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end,
                                   int index) {
        // TODO: I think locals could be handled better than this
        MapNode localNode = new LinkedHashMapNode();
        localNode.put(NodeConstants.NAME, new ValueNode(name));
        localNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        localNode.put(NodeConstants.SIGNATURE, new ValueNode(signature));
        localNode.put(NodeConstants.START, new ValueNode(start.toString()));
        localNode.put(NodeConstants.END, new ValueNode(end.toString()));
        localNode.put(NodeConstants.INDEX, new ValueNode(index));
        locals.add(localNode);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
                                                          int[] index, String descriptor, boolean visible) {
        // TODO: How to handle this?
        return null;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        MapNode lineNode = new LinkedHashMapNode();
        lineNode.put(NodeConstants.LINE, new ValueNode(line));
        lineNode.put(NodeConstants.LABEL, new ValueNode(start.toString()));
        lineNumbers.add(lineNode);
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
