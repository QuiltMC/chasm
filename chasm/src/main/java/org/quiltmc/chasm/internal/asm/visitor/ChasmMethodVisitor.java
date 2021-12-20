package org.quiltmc.chasm.internal.asm.visitor;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.quiltmc.chasm.api.tree.ArrayListNode;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.api.util.ClassInfoProvider;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChasmMethodVisitor extends MethodVisitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChasmMethodVisitor.class);

    private final ClassInfoProvider classInfoProvider;
    private final Type currentClass;
    private final Type currentSuperClass;
    private final List<Type> currentInterfaces;
    private final boolean currentClassIsInterface;

    private final MapNode methodNode;

    private final ListNode parameters = new ArrayListNode();

    private final ListNode annotations = new ArrayListNode();
    private final ListNode attributes = new ArrayListNode();

    private final MapNode code = new LinkedHashMapNode();
    private final ListNode instructions = new ArrayListNode();
    private final ListNode sourceLocals = new ArrayListNode();
    private final ListNode tryCatchBlocks = new ArrayListNode();

    private final ListNode lineNumbers = new ArrayListNode();

    private int visitedParameterCount = 0;
    private int parameterAnnotationOffset = 0;
    private int visibleParameterAnnotationOffset = 0;
    private final Map<Label, String> labelIds = new HashMap<>();
    private final Map<AbstractInsnNode, MapNode> localVariableSensitiveInstructions = new HashMap<>();

    public ChasmMethodVisitor(int api, ClassInfoProvider classInfoProvider, Type currentClass,
                              Type currentSuperClass, List<Type> currentInterfaces,
                              boolean currentClassIsInterface, MapNode methodNode, int access, String name,
                              String descriptor, String signature,
                              String[] exceptions) {
        super(api, new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions));
        this.classInfoProvider = classInfoProvider;
        this.currentClass = currentClass;
        this.currentSuperClass = currentSuperClass;
        this.currentInterfaces = currentInterfaces;
        this.currentClassIsInterface = currentClassIsInterface;
        this.methodNode = methodNode;

        methodNode.put(NodeConstants.ACCESS, new ValueNode(access));
        methodNode.put(NodeConstants.NAME, new ValueNode(name));

        methodNode.put(NodeConstants.PARAMETERS, parameters);
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        for (int i = 0; i < argumentTypes.length; i++) {
            MapNode parameterNode = new LinkedHashMapNode();
            parameterNode.put(NodeConstants.TYPE, new ValueNode(argumentTypes[i]));
            parameterNode.put(NodeConstants.NAME, new ValueNode("P" + i));
            this.parameters.add(parameterNode);
        }

        Type returnType = Type.getReturnType(descriptor);
        methodNode.put(NodeConstants.RETURN_TYPE, new ValueNode(returnType));

        methodNode.put(NodeConstants.SIGNATURE, new ValueNode(signature));

        ListNode exceptionsNode = new ArrayListNode();
        if (exceptions != null) {
            for (String exception : exceptions) {
                exceptionsNode.add(new ValueNode(exception));
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
            parameterNode.put(NodeConstants.NAME, new ValueNode(name));
        }
        parameterNode.put(NodeConstants.ACCESS, new ValueNode(access));
        super.visitParameter(name, access);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        // We simply right-align the annotations, assuming that non-annotable parameters are always at the start.
        if (visible) {
            visibleParameterAnnotationOffset = this.parameters.size() - parameterCount;
        } else {
            parameterAnnotationOffset = this.parameters.size() - parameterCount;
        }

        super.visitAnnotableParameterCount(parameterCount, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);

        int actualIndex = parameter + (visible ? visibleParameterAnnotationOffset : parameterAnnotationOffset);
        MapNode parameterNode = Node.asMap(this.parameters.get(actualIndex));
        ListNode annotations =
                Node.asList(parameterNode.computeIfAbsent(NodeConstants.ANNOTATIONS, s -> new ArrayListNode()));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values, super.visitParameterAnnotation(parameter, descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        MapNode annotationDefault = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        methodNode.put(NodeConstants.ANNOTATION_DEFAULT, annotationDefault);
        methodNode.put(NodeConstants.VALUES, values);

        return new ChasmAnnotationVisitor(api, values, super.visitAnnotationDefault());
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, values);
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values, super.visitAnnotation(descriptor, visible));
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

        return new ChasmAnnotationVisitor(api, values, super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        attributes.add(new ValueNode(attribute));
        super.visitAttribute(attribute);
    }

    @Override
    public void visitCode() {
        methodNode.put(NodeConstants.CODE, code);
        code.put(NodeConstants.INSTRUCTIONS, instructions);
        code.put(NodeConstants.SOURCE_LOCALS, sourceLocals);
        code.put(NodeConstants.TRY_CATCH_BLOCKS, tryCatchBlocks);
        code.put(NodeConstants.LINE_NUMBERS, lineNumbers);
        super.visitCode();
    }

    @Override
    public void visitLabel(Label label) {
        MapNode labelNode = new LinkedHashMapNode();
        labelNode.put(NodeConstants.LABEL, new ValueNode(getLabelId(label)));
        instructions.add(labelNode);
        super.visitLabel(label);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        // Don't care, ClassReader is set to SKIP_FRAMES and ClassWriter to COMPUTE_FRAMES
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitInsn(int opcode) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructions.add(instructionNode);
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.OPERAND, new ValueNode(operand));
        instructions.add(instructionNode);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructions.add(instructionNode);
        super.visitVarInsn(opcode, var);
        localVariableSensitiveInstructions.put(((MethodNode) mv).instructions.getLast(), instructionNode);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.TYPE, new ValueNode(type));
        instructions.add(instructionNode);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(opcode));
        instructionNode.put(NodeConstants.OWNER, new ValueNode(owner));
        instructionNode.put(NodeConstants.NAME, new ValueNode(name));
        instructionNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        instructions.add(instructionNode);
        super.visitFieldInsn(opcode, owner, name, descriptor);
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
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
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
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
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
        instructionNode.put(NodeConstants.TARGET, new ValueNode(getLabelId(label)));
        instructions.add(instructionNode);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.LDC));
        instructionNode.put(NodeConstants.VALUE, new ValueNode(value));
        instructions.add(instructionNode);
        super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.IINC));
        instructionNode.put(NodeConstants.INCREMENT, new ValueNode(increment));
        instructions.add(instructionNode);
        super.visitIincInsn(var, increment);
        localVariableSensitiveInstructions.put(((MethodNode) mv).instructions.getLast(), instructionNode);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.TABLESWITCH));
        instructionNode.put(NodeConstants.DEFAULT, new ValueNode(getLabelId(dflt)));
        ListNode cases = new ArrayListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put(NodeConstants.KEY, new ValueNode(min + i));
            caseNode.put(NodeConstants.LABEL, new ValueNode(getLabelId(labels[i])));
            cases.add(caseNode);
        }
        instructionNode.put(NodeConstants.CASES, cases);
        instructions.add(instructionNode);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.LOOKUPSWITCH));
        instructionNode.put(NodeConstants.DEFAULT, new ValueNode(getLabelId(dflt)));
        ListNode cases = new ArrayListNode();
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new LinkedHashMapNode();
            caseNode.put(NodeConstants.KEY, new ValueNode(keys[i]));
            caseNode.put(NodeConstants.LABEL, new ValueNode(getLabelId(labels[i])));
            cases.add(caseNode);
        }
        instructionNode.put(NodeConstants.CASES, cases);
        instructions.add(instructionNode);

        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        MapNode instructionNode = new LinkedHashMapNode();
        instructionNode.put(NodeConstants.OPCODE, new ValueNode(Opcodes.MULTIANEWARRAY));
        instructionNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        instructionNode.put(NodeConstants.DIMENSIONS, new ValueNode(numDimensions));
        instructions.add(instructionNode);

        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode instructionNode = Node.asMap(instructions.get(instructions.size() - 1));

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, new ValueNode(values));
        annotation.put(NodeConstants.TYPE_REF, new ValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new ValueNode(typePath.toString()));

        ListNode annotations = Node.asList(Node.asMap(instructionNode)
                .computeIfAbsent(NodeConstants.ANNOTATIONS, s -> new ArrayListNode()));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values, super.visitInsnAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        MapNode tryCatchBlock = new LinkedHashMapNode();
        tryCatchBlock.put(NodeConstants.START, new ValueNode(getLabelId(start)));
        tryCatchBlock.put(NodeConstants.END, new ValueNode(getLabelId(end)));
        tryCatchBlock.put(NodeConstants.HANDLER, new ValueNode(getLabelId(handler)));
        tryCatchBlock.put(NodeConstants.TYPE, new ValueNode(type));
        tryCatchBlock.put(NodeConstants.ANNOTATIONS, new ArrayListNode());
        tryCatchBlocks.add(tryCatchBlock);

        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor,
                                                     boolean visible) {
        MapNode tryCatchBlock = Node.asMap(tryCatchBlocks.get(tryCatchBlocks.size() - 1));

        MapNode annotation = new LinkedHashMapNode();
        ListNode values = new ArrayListNode();
        annotation.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        annotation.put(NodeConstants.VISIBLE, new ValueNode(visible));
        annotation.put(NodeConstants.VALUES, new ValueNode(values));
        annotation.put(NodeConstants.TYPE_REF, new ValueNode(typeRef));
        annotation.put(NodeConstants.TYPE_PATH, new ValueNode(typePath.toString()));

        ListNode annotations = Node.asList(tryCatchBlock.get(NodeConstants.ANNOTATIONS));
        annotations.add(annotation);

        return new ChasmAnnotationVisitor(api, values, super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        MapNode localNode = new LinkedHashMapNode();
        localNode.put(NodeConstants.NAME, new ValueNode(name));
        localNode.put(NodeConstants.DESCRIPTOR, new ValueNode(descriptor));
        localNode.put(NodeConstants.SIGNATURE, new ValueNode(signature));
        localNode.put(NodeConstants.START, new ValueNode(getLabelId(start)));
        localNode.put(NodeConstants.END, new ValueNode(getLabelId(end)));
        // TODO: what should we do about this index?
        localNode.put(NodeConstants.INDEX, new ValueNode(index));
        sourceLocals.add(localNode);

        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
                                                          int[] index, String descriptor, boolean visible) {
        // TODO: How to handle this?
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        MapNode lineNode = new LinkedHashMapNode();
        lineNode.put(NodeConstants.LINE, new ValueNode(line));
        lineNode.put(NodeConstants.LABEL, new ValueNode(getLabelId(start)));
        lineNumbers.add(lineNode);
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // Don't care, ClassWriter is set to COMPUTE_MAXS
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        // TODO: Attach final labels to a NOP
        visitInsn(Opcodes.NOP);
        super.visitEnd();

        computeLocalVariables();
    }

    private String getLabelId(Label label) {
        return labelIds.computeIfAbsent(label, l -> "L" + labelIds.size());
    }

    private void computeLocalVariables() {
        MethodNode method = (MethodNode) this.mv;
        Analyzer<LocalValue> analyzer = new Analyzer<>(new LocalInterpreter(method, classInfoProvider, currentClass, currentSuperClass, currentInterfaces, currentClassIsInterface));
        Frame<LocalValue>[] frames;
        try {
            frames = analyzer.analyzeAndComputeMaxs(currentClass.getInternalName(), method);
        } catch (Throwable e) {
            LOGGER.error("Error analyzing method " + method.name + method.desc, e);
            return;
        }

        int[] equivalentStores = computeEquivalentStores(method, frames);
        int argumentSize = Type.getArgumentsAndReturnSizes(method.desc) >> 2;
        if ((method.access & Opcodes.ACC_STATIC) != 0) {
            argumentSize--;
        }
        int[] paramIndexes = new int[argumentSize];
        int index = (method.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        int argIndex = 0;
        for (Type argType : Type.getArgumentTypes(method.desc)) {
            paramIndexes[index] = argIndex++;
            index += argType.getSize();
        }

        localVariableSensitiveInstructions.forEach((asmInsn, chasmInsn) -> {
            int opcode = asmInsn.getOpcode();
            int varIndex = opcode == Opcodes.IINC ? ((IincInsnNode) asmInsn).var : ((VarInsnNode) asmInsn).var;
            @Nullable String localLabel;
            if (varIndex < paramIndexes.length) {
                if (varIndex == 0 && (method.access & Opcodes.ACC_STATIC) == 0) {
                    localLabel = "this";
                } else {
                    localLabel = "P" + paramIndexes[varIndex];
                }
            } else {
                localLabel = null;
                int frameIndex = method.instructions.indexOf(asmInsn);
                if (opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE) {
                    frameIndex++;
                }
                if (frameIndex < frames.length) {
                    Frame<LocalValue> frame = frames[frameIndex];
                    if (frame != null) {
                        LocalValue value = frame.getLocal(varIndex);
                        int[] sourceStores = value.getSourceStores();
                        if (sourceStores != null) {
                            int sourceStore = sourceStores[0];
                            if (equivalentStores[sourceStore] != 0) {
                                sourceStore = equivalentStores[sourceStore];
                            }
                            localLabel = "V" + sourceStore;
                        }
                    }
                }
            }
            if (localLabel != null) {
                chasmInsn.put(NodeConstants.VAR, new ValueNode(localLabel));
            }
        });
    }

    private int[] computeEquivalentStores(MethodNode method, Frame<LocalValue>[] frames) {
        // This is initialized to zero. Zero means there is no other equivalent store.
        // We rely on the fact that the first instruction cannot be a store, since it would pop a non-existent value off the stack.
        int[] equivalentStores = new int[frames.length];

        for (AbstractInsnNode insn : method.instructions) {
            int opcode = insn.getOpcode();
            int index = method.instructions.indexOf(insn);
            if (opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE) {
                if (index + 1 < frames.length) {
                    Frame<LocalValue> frame = frames[index + 1];
                    if (frame != null) {
                        int[] sourceStoresHere = frame.getLocal(((VarInsnNode) insn).var).getSourceStores();
                        findEquivalentSourceStores(equivalentStores, index, sourceStoresHere);
                    }
                }
            } else if (opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD || opcode == Opcodes.RET) {
                Frame<LocalValue> frame = frames[index];
                if (frame != null) {
                    int[] sourceStoresHere = frame.getLocal(((VarInsnNode) insn).var).getSourceStores();
                    findEquivalentSourceStores(equivalentStores, index, sourceStoresHere);
                }
            } else if (opcode == Opcodes.IINC) {
                Frame<LocalValue> frame = frames[index];
                if (frame != null) {
                    int[] sourceStoresHere = frame.getLocal(((IincInsnNode) insn).var).getSourceStores();
                    findEquivalentSourceStores(equivalentStores, index, sourceStoresHere);
                }
            }
        }
        return equivalentStores;
    }

    private void findEquivalentSourceStores(int[] sameStores, int index, int[] sourceStoresHere) {
        if (sourceStoresHere != null) {
            int equivalentStore = sameStores[index];
            if (equivalentStore == 0) {
                equivalentStore = sourceStoresHere[0];
            }
            for (int sourceStore : sourceStoresHere) {
                sameStores[sourceStore] = equivalentStore;
            }
        }
    }

    private MapNode makeLocal(int index, Type type) {
        MapNode node = new LinkedHashMapNode();
        node.put(NodeConstants.INDEX, new ValueNode(index));
        node.put(NodeConstants.TYPE, new ValueNode(type));
        return node;
    }
}
