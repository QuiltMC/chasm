package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.WrapperValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmMethodVisitor extends MethodVisitor {
    private final MapNode methodNode;

    private final ListNode parameters = new ArrayListNode();

    private final ListNode annotations = new ArrayListNode();
    private final ListNode attributes = new ArrayListNode();

    private final MapNode code = new LinkedHashMapNode();
    private final ListNode instructions = new ArrayListNode();
    private final ListNode locals = new ArrayListNode();
    private final ListNode tryCatchBlocks = new ArrayListNode();

    private final ListNode lineNumbers = new ArrayListNode();

    private int visitedParameterCount = 0;
    private int parameterAnnotationOffset = 0;
    private int visibleParameterAnnotationOffset = 0;

    public ChasmMethodVisitor(int api, MapNode methodNode, int access, String name, String descriptor, String signature,
                              String[] exceptions) {
        super(api);
        this.methodNode = methodNode;

        methodNode.put(NodeConstants.ACCESS, new WrapperValueNode(access));
        methodNode.put(NodeConstants.NAME, new WrapperValueNode(name));

        methodNode.put(NodeConstants.PARAMETERS, parameters);
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        for (int i = 0; i < argumentTypes.length; i++) {
            MapNode parameterNode = new LinkedHashMapNode();
            parameterNode.put(NodeConstants.TYPE, new WrapperValueNode(argumentTypes[i]));
            parameterNode.put(NodeConstants.NAME, new WrapperValueNode("arg" + i));
            this.parameters.add(parameterNode);
        }

        Type returnType = Type.getReturnType(descriptor);
        methodNode.put(NodeConstants.RETURN_TYPE, new WrapperValueNode(returnType));

        methodNode.put(NodeConstants.SIGNATURE, new WrapperValueNode(signature));

        ListNode exceptionsNode = new ArrayListNode();
        if (exceptions != null) {
            for (String exception : exceptions) {
                exceptionsNode.add(new WrapperValueNode(exception));
            }
        }
        methodNode.put(NodeConstants.EXCEPTIONS, exceptionsNode);

        methodNode.put(NodeConstants.ANNOTATIONS, annotations);
        methodNode.put(NodeConstants.ATTRIBUTES, attributes);
    }

    @Override
    public void visitParameter(String name, int access) {
        MapNode parameterNode = Node.asMap(this.parameters.get(visitedParameterCount++));
        if (name != null) {
            parameterNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        }
        parameterNode.put(NodeConstants.ACCESS, new WrapperValueNode(access));
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        // We simply right-align the annotations, assuming that non-annotable parameters are always at the start.
        if (visible) {
            visibleParameterAnnotationOffset = this.parameters.size() - parameterCount;
        } else {
            parameterAnnotationOffset = this.parameters.size() - parameterCount;
        }
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);

        int actualIndex = parameter + (visible ? visibleParameterAnnotationOffset : parameterAnnotationOffset);
        MapNode parameterNode = Node.asMap(this.parameters.get(actualIndex));
        ListNode annotations =
                Node.asList(parameterNode.computeIfAbsent(NodeConstants.ANNOTATIONS, s -> new ArrayListNode()));
        annotations.add(annotation);

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
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.TYPE_REF, new WrapperValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new WrapperValueNode(typePath.toString()));
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, new WrapperValueNode(values));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new WrapperValueNode(attribute));
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
        labelNode.put(NodeConstants.LABEL, new WrapperValueNode(label.toString()));
        instructions.add(labelNode);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        // Don't care, ClassReader is set to SKIP_FRAMES and ClassWriter to COMPUTE_FRAMES
    }

    @Override
    public void visitInsn(int opcode) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(opcode));
        instructions.add(instructionNode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(opcode));
        instructionNode.put(NodeConstants.OPERAND, new WrapperValueNode(operand));
        instructions.add(instructionNode);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(opcode));
        instructionNode.put(NodeConstants.VAR, new WrapperValueNode(var));
        instructions.add(instructionNode);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(opcode));
        instructionNode.put(NodeConstants.TYPE, new WrapperValueNode(type));
        instructions.add(instructionNode);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(opcode));
        instructionNode.put(NodeConstants.OWNER, new WrapperValueNode(owner));
        instructionNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        instructionNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        instructions.add(instructionNode);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(opcode));
        instructionNode.put(NodeConstants.OWNER, new WrapperValueNode(owner));
        instructionNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        instructionNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        instructionNode.put(NodeConstants.IS_INTERFACE, new WrapperValueNode(isInterface));
        instructions.add(instructionNode);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(Opcodes.INVOKEDYNAMIC));
        instructionNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        instructionNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        instructionNode.put(NodeConstants.HANDLE, getHandleNode(bootstrapMethodHandle));
        instructionNode.put(NodeConstants.ARGUMENTS, getArgumentsNode(bootstrapMethodArguments));
        instructions.add(instructionNode);
    }

    private MapNode getHandleNode(Handle handle) {
        MapNode handleNode = new LinkedHashMapNode();
        handleNode.put(NodeConstants.TAG, new WrapperValueNode(handle.getTag()));
        handleNode.put(NodeConstants.OWNER, new WrapperValueNode(handle.getOwner()));
        handleNode.put(NodeConstants.NAME, new WrapperValueNode(handle.getName()));
        handleNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(handle.getDesc()));
        handleNode.put(NodeConstants.IS_INTERFACE, new WrapperValueNode(handle.isInterface()));
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
                constDynamicNode.put(NodeConstants.NAME, new WrapperValueNode(constantDynamic.getName()));
                constDynamicNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(constantDynamic.getDescriptor()));
                constDynamicNode.put(NodeConstants.HANDLE, getHandleNode(constantDynamic.getBootstrapMethod()));
                Object[] arguments = new Object[constantDynamic.getBootstrapMethodArgumentCount()];
                for (int i = 0; i < arguments.length; i++) {
                    arguments[i] = constantDynamic.getBootstrapMethodArgument(i);
                }
                constDynamicNode.put(NodeConstants.ARGUMENTS, getArgumentsNode(arguments));
                argumentsNode.add(constDynamicNode);
            } else {
                argumentsNode.add(new WrapperValueNode(arg));
            }
        }
        return argumentsNode;
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(opcode));
        instructionNode.put(NodeConstants.TARGET, new WrapperValueNode(label.toString()));
        instructions.add(instructionNode);
    }

    @Override
    public void visitLdcInsn(Object value) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(Opcodes.LDC));
        instructionNode.put(NodeConstants.VALUE, new WrapperValueNode(value));
        instructions.add(instructionNode);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(Opcodes.IINC));
        instructionNode.put(NodeConstants.VAR, new WrapperValueNode(var));
        instructionNode.put(NodeConstants.INCREMENT, new WrapperValueNode(increment));
        instructions.add(instructionNode);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(Opcodes.TABLESWITCH));
        instructionNode.put(NodeConstants.DEFAULT, new WrapperValueNode(dflt.toString()));
        ListNode cases = new ArrayListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put(NodeConstants.KEY, new WrapperValueNode(min + i));
            caseNode.put(NodeConstants.LABEL, new WrapperValueNode(labels[i].toString()));
            cases.add(caseNode);
        }
        instructionNode.put(NodeConstants.CASES, cases);
        instructions.add(instructionNode);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(Opcodes.LOOKUPSWITCH));
        instructionNode.put(NodeConstants.DEFAULT, new WrapperValueNode(dflt.toString()));
        ListNode cases = new ArrayListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put(NodeConstants.KEY, new WrapperValueNode(keys[i]));
            caseNode.put(NodeConstants.LABEL, new WrapperValueNode(labels[i].toString()));
            cases.add(caseNode);
        }
        instructionNode.put(NodeConstants.CASES, cases);
        instructions.add(instructionNode);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new WrapperValueNode(Opcodes.MULTIANEWARRAY));
        instructionNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        instructionNode.put(NodeConstants.DIMENSIONS, new WrapperValueNode(numDimensions));
        instructions.add(instructionNode);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode instructionNode = Node.asMap(instructions.get(instructions.size() - 1));

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, new WrapperValueNode(values));
        annotation.put(NodeConstants.TYPE_REF, new WrapperValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new WrapperValueNode(typePath.toString()));

        ListNode annotations = Node.asList(Node.asMap(instructionNode)
                .computeIfAbsent(NodeConstants.ANNOTATIONS, s -> new ArrayListNode()));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        MapNode tryCatchBlock = new LinkedHashMapNode();
        tryCatchBlock.put(NodeConstants.START, new WrapperValueNode(start.toString()));
        tryCatchBlock.put(NodeConstants.END, new WrapperValueNode(end.toString()));
        tryCatchBlock.put(NodeConstants.HANDLER, new WrapperValueNode(handler.toString()));
        tryCatchBlock.put(NodeConstants.TYPE, new WrapperValueNode(type));
        tryCatchBlock.put(NodeConstants.ANNOTATIONS, new ArrayListNode());
        tryCatchBlocks.add(tryCatchBlock);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor,
                                                     boolean visible) {
        MapNode tryCatchBlock = Node.asMap(tryCatchBlocks.get(tryCatchBlocks.size() - 1));

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new WrapperValueNode(visible));
        annotation.put(NodeConstants.VALUES, new WrapperValueNode(values));
        annotation.put(NodeConstants.TYPE_REF, new WrapperValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new WrapperValueNode(typePath.toString()));

        ListNode annotations = Node.asList(tryCatchBlock.get(NodeConstants.ANNOTATIONS));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end,
                                   int index) {
        // TODO: I think locals could be handled better than this
        MapNode localNode = new LinkedHashMapNode();
        localNode.put(NodeConstants.NAME, new WrapperValueNode(name));
        localNode.put(NodeConstants.DESCRIPTOR, new WrapperValueNode(descriptor));
        localNode.put(NodeConstants.SIGNATURE, new WrapperValueNode(signature));
        localNode.put(NodeConstants.START, new WrapperValueNode(start.toString()));
        localNode.put(NodeConstants.END, new WrapperValueNode(end.toString()));
        localNode.put(NodeConstants.INDEX, new WrapperValueNode(index));
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
        lineNode.put(NodeConstants.LINE, new WrapperValueNode(line));
        lineNode.put(NodeConstants.LABEL, new WrapperValueNode(start.toString()));
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
