package org.quiltmc.chasm.internal.asm.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.BooleanNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChasmMethodVisitor extends MethodVisitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChasmMethodVisitor.class);

    private final Context context;
    private final boolean isStatic;
    private final String descriptor;
    private final Type currentClass;
    private final Type currentSuperClass;
    private final List<Type> currentInterfaces;
    private final boolean currentClassIsInterface;

    private final MapNode methodNode;

    private final ListNode annotations = new ListNode(new ArrayList<>());

    private final MapNode code = new MapNode(new LinkedHashMap<>());
    private final ListNode instructions = new ListNode(new ArrayList<>());
    private final MapNode locals = Ast.emptyMap();
    private final ListNode tryCatchBlocks = new ListNode(new ArrayList<>());

    private final ListNode lineNumbers = new ListNode(new ArrayList<>());

    private int visitedParameterCount = 0;
    private int parameterAnnotationOffset = 0;
    private int visibleParameterAnnotationOffset = 0;
    private final Map<Label, String> labelIds = new HashMap<>();
    private final Map<AbstractInsnNode, MapNode> localVariableSensitiveInstructions = new HashMap<>();
    private boolean hasCode = false;

    public ChasmMethodVisitor(int api, Context context, Type currentClass,
                              Type currentSuperClass, List<Type> currentInterfaces,
                              boolean currentClassIsInterface, MapNode methodNode, int access, String name,
                              String descriptor, String signature,
                              String[] exceptions) {
        super(api, new MethodNode(Opcodes.ASM9, access, name, descriptor, signature, exceptions));
        this.context = context;
        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
        this.descriptor = descriptor;
        this.currentClass = currentClass;
        this.currentSuperClass = currentSuperClass;
        this.currentInterfaces = currentInterfaces;
        this.currentClassIsInterface = currentClassIsInterface;
        this.methodNode = methodNode;

        methodNode.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        methodNode.getEntries().put(NodeConstants.NAME, new StringNode(name));

        methodNode.getEntries().put(NodeConstants.LOCALS, this.locals);

        if ((access & Opcodes.ACC_STATIC) == 0) {
            MapNode thisVar = Ast.map().put(NodeConstants.TYPE, currentClass.getDescriptor()).build();
            this.locals.getEntries().put("this", thisVar);
        }

        ListNode params = Ast.emptyList();
        methodNode.getEntries().put(NodeConstants.PARAMETERS, params);
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        for (int i = 0; i < argumentTypes.length; i++) {
            MapNode param = Ast.map().put(NodeConstants.TYPE, argumentTypes[i].getDescriptor()).build();
            this.locals.getEntries().put("P" + i, param);
            params.getEntries().add(Ast.literal("P" + i));
        }

        Type returnType = Type.getReturnType(descriptor);
        methodNode.getEntries().put(NodeConstants.RETURN_TYPE, new StringNode(returnType.toString()));

        methodNode.getEntries().put(NodeConstants.SIGNATURE, new StringNode(signature));

        ListNode exceptionsNode = new ListNode(new ArrayList<>());
        if (exceptions != null) {
            for (String exception : exceptions) {
                exceptionsNode.getEntries().add(new StringNode(exception));
            }
        }
        methodNode.getEntries().put(NodeConstants.EXCEPTIONS, exceptionsNode);

        methodNode.getEntries().put(NodeConstants.ANNOTATIONS, annotations);
    }

    @Override
    public void visitParameter(String name, int access) {
        MapNode parameterNode = (MapNode) this.locals.getEntries().get("P" + visitedParameterCount++);
        if (parameterNode != null) {
            if (name != null) {
                parameterNode.getEntries().put(NodeConstants.NAME, Ast.literal(name));
            }
            parameterNode.getEntries().put(NodeConstants.ACCESS, Ast.literal(access));
        }
        super.visitParameter(name, access);
    }

    @Override
    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        // We simply right-align the annotations, assuming that non-annotable parameters are always at the start.
        int totalNumParameters = (int) this.locals.getEntries().keySet().stream()
                .filter(it -> it.startsWith("P")).count();
        if (visible) {
            visibleParameterAnnotationOffset = totalNumParameters - parameterCount;
        } else {
            parameterAnnotationOffset = totalNumParameters - parameterCount;
        }

        super.visitAnnotableParameterCount(parameterCount, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);

        int actualIndex = parameter + (visible ? visibleParameterAnnotationOffset : parameterAnnotationOffset);
        MapNode parameterNode = (MapNode) this.locals.getEntries().get("P" + actualIndex);
        ListNode annotations = (ListNode) (parameterNode.getEntries()
                .computeIfAbsent(NodeConstants.ANNOTATIONS, s -> new ListNode(new ArrayList<>())));
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(api, values, super.visitParameterAnnotation(parameter, descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        ListNode values = new ListNode(new ArrayList<>());
        methodNode.getEntries().put(NodeConstants.ANNOTATION_DEFAULT, values);

        return new ChasmAnnotationVisitor(api, values, super.visitAnnotationDefault());
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(api, values, super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.TYPE_REF, new IntegerNode(typeRef));
        annotation.getEntries().put(NodeConstants.TYPE_PATH, new StringNode(typePath.toString()));
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(
                api,
                values,
                super.visitTypeAnnotation(typeRef, typePath, descriptor, visible)
        );
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        throw new RuntimeException("Unknown attribute: " + attribute.type);
    }

    @Override
    public void visitCode() {
        hasCode = true;
        methodNode.getEntries().put(NodeConstants.CODE, code);
        code.getEntries().put(NodeConstants.INSTRUCTIONS, instructions);
        code.getEntries().put(NodeConstants.TRY_CATCH_BLOCKS, tryCatchBlocks);
        code.getEntries().put(NodeConstants.LINE_NUMBERS, lineNumbers);
        super.visitCode();
    }

    @Override
    public void visitLabel(Label label) {
        MapNode labelNode = new MapNode(new LinkedHashMap<>());
        labelNode.getEntries().put(NodeConstants.LABEL, new StringNode(getLabelId(label)));
        instructions.getEntries().add(labelNode);
        super.visitLabel(label);
    }

    @Override
    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        // Don't care, ClassReader is set to SKIP_FRAMES and ClassWriter to COMPUTE_FRAMES
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitInsn(int opcode) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(opcode));
        instructions.getEntries().add(instructionNode);
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(opcode));
        instructionNode.getEntries().put(NodeConstants.OPERAND, new IntegerNode(operand));
        instructions.getEntries().add(instructionNode);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(opcode));
        instructions.getEntries().add(instructionNode);
        super.visitVarInsn(opcode, var);
        localVariableSensitiveInstructions.put(((MethodNode) mv).instructions.getLast(), instructionNode);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(opcode));
        instructionNode.getEntries().put(NodeConstants.TYPE, new StringNode(type));
        instructions.getEntries().add(instructionNode);
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(opcode));
        instructionNode.getEntries().put(NodeConstants.OWNER, new StringNode(owner));
        instructionNode.getEntries().put(NodeConstants.NAME, new StringNode(name));
        instructionNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        instructions.getEntries().add(instructionNode);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(opcode));
        instructionNode.getEntries().put(NodeConstants.OWNER, new StringNode(owner));
        instructionNode.getEntries().put(NodeConstants.NAME, new StringNode(name));
        instructionNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        instructionNode.getEntries().put(NodeConstants.IS_INTERFACE, BooleanNode.from(isInterface));
        instructions.getEntries().add(instructionNode);
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                       Object... bootstrapMethodArguments) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(Opcodes.INVOKEDYNAMIC));
        instructionNode.getEntries().put(NodeConstants.NAME, new StringNode(name));
        instructionNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        instructionNode.getEntries().put(NodeConstants.HANDLE, NodeUtils.getHandleNode(bootstrapMethodHandle));
        instructionNode.getEntries().put(NodeConstants.ARGUMENTS, NodeUtils.getValueListNode(bootstrapMethodArguments));
        instructions.getEntries().add(instructionNode);
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }


    @Override
    public void visitJumpInsn(int opcode, Label label) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(opcode));
        instructionNode.getEntries().put(NodeConstants.TARGET, new StringNode(getLabelId(label)));
        instructions.getEntries().add(instructionNode);
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(Opcodes.LDC));
        instructionNode.getEntries().put(NodeConstants.VALUE, NodeUtils.getValueNode(value));
        instructions.getEntries().add(instructionNode);
        super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(Opcodes.IINC));
        instructionNode.getEntries().put(NodeConstants.INCREMENT, new IntegerNode(increment));
        instructions.getEntries().add(instructionNode);
        super.visitIincInsn(var, increment);
        localVariableSensitiveInstructions.put(((MethodNode) mv).instructions.getLast(), instructionNode);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(Opcodes.TABLESWITCH));
        instructionNode.getEntries().put(NodeConstants.DEFAULT, new StringNode(getLabelId(dflt)));
        ListNode cases = new ListNode(new ArrayList<>());
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new MapNode(new LinkedHashMap<>());
            caseNode.getEntries().put(NodeConstants.KEY, new IntegerNode(min + i));
            caseNode.getEntries().put(NodeConstants.LABEL, new StringNode(getLabelId(labels[i])));
            cases.getEntries().add(caseNode);
        }
        instructionNode.getEntries().put(NodeConstants.CASES, cases);
        instructions.getEntries().add(instructionNode);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(Opcodes.LOOKUPSWITCH));
        instructionNode.getEntries().put(NodeConstants.DEFAULT, new StringNode(getLabelId(dflt)));
        ListNode cases = new ListNode(new ArrayList<>());
        for (int i = 0; i < labels.length; i++) {
            MapNode caseNode = new MapNode(new LinkedHashMap<>());
            caseNode.getEntries().put(NodeConstants.KEY, new IntegerNode(keys[i]));
            caseNode.getEntries().put(NodeConstants.LABEL, new StringNode(getLabelId(labels[i])));
            cases.getEntries().add(caseNode);
        }
        instructionNode.getEntries().put(NodeConstants.CASES, cases);
        instructions.getEntries().add(instructionNode);

        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        MapNode instructionNode = new MapNode(new LinkedHashMap<>());
        instructionNode.getEntries().put(NodeConstants.OPCODE, new IntegerNode(Opcodes.MULTIANEWARRAY));
        instructionNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        instructionNode.getEntries().put(NodeConstants.DIMENSIONS, new IntegerNode(numDimensions));
        instructions.getEntries().add(instructionNode);

        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        MapNode instructionNode = (MapNode) instructions.getEntries().get(instructions.getEntries().size() - 1);

        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotation.getEntries().put(NodeConstants.TYPE_REF, new IntegerNode(typeRef));
        annotation.getEntries().put(NodeConstants.TYPE_PATH, new StringNode(typePath.toString()));

        ListNode annotations = (ListNode) instructionNode.getEntries()
                .computeIfAbsent(NodeConstants.ANNOTATIONS, s -> new ListNode(new ArrayList<>()));
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(
                api,
                values,
                super.visitInsnAnnotation(typeRef, typePath, descriptor, visible)
        );
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        MapNode tryCatchBlock = new MapNode(new LinkedHashMap<>());
        tryCatchBlock.getEntries().put(NodeConstants.START, new StringNode(getLabelId(start)));
        tryCatchBlock.getEntries().put(NodeConstants.END, new StringNode(getLabelId(end)));
        tryCatchBlock.getEntries().put(NodeConstants.HANDLER, new StringNode(getLabelId(handler)));
        tryCatchBlock.getEntries().put(NodeConstants.TYPE, new StringNode(type));
        tryCatchBlock.getEntries().put(NodeConstants.ANNOTATIONS, new ListNode(new ArrayList<>()));
        tryCatchBlocks.getEntries().add(tryCatchBlock);

        super.visitTryCatchBlock(start, end, handler, type);
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor,
                                                     boolean visible) {
        MapNode tryCatchBlock = (MapNode) tryCatchBlocks.getEntries().get(tryCatchBlocks.getEntries().size() - 1);

        MapNode annotation = new MapNode(new LinkedHashMap<>());
        MapNode values = new MapNode(new LinkedHashMap<>());
        annotation.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(descriptor));
        annotation.getEntries().put(NodeConstants.VISIBLE, BooleanNode.from(visible));
        annotation.getEntries().put(NodeConstants.VALUES, values);
        annotation.getEntries().put(NodeConstants.TYPE_REF, new IntegerNode(typeRef));
        annotation.getEntries().put(NodeConstants.TYPE_PATH, new StringNode(typePath.toString()));

        ListNode annotations = (ListNode) tryCatchBlock.getEntries().get(NodeConstants.ANNOTATIONS);
        annotations.getEntries().add(annotation);

        return new ChasmAnnotationVisitor(
                api,
                values,
                super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible)
        );
    }

    @Override
    public void visitLocalVariable(
            String name,
            String descriptor,
            String signature,
            Label start,
            Label end,
            int index
    ) {
        // We do the local variables at the end
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end,
                                                          int[] index, String descriptor, boolean visible) {
        // We do the local variables at the end
        return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        MapNode lineNode = new MapNode(new LinkedHashMap<>());
        lineNode.getEntries().put(NodeConstants.LINE, new IntegerNode(line));
        lineNode.getEntries().put(NodeConstants.LABEL, new StringNode(getLabelId(start)));
        lineNumbers.getEntries().add(lineNode);
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // Don't care, ClassWriter is set to COMPUTE_MAXS
        super.visitMaxs(maxStack, maxLocals);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (hasCode) {
            computeLocalVariables();
            handleSourceLocals();
        }
    }

    private String getLabelId(Label label) {
        return labelIds.computeIfAbsent(label, l -> "L" + labelIds.size());
    }

    private void computeLocalVariables() {
        MethodNode method = (MethodNode) this.mv;
        LocalInterpreter interpreter = new LocalInterpreter(
                method,
                context,
                currentClass,
                currentSuperClass,
                currentInterfaces,
                currentClassIsInterface
        );
        Analyzer<LocalValue> analyzer = new Analyzer<>(interpreter);
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

        // Build up a sorted list of local variables to add to the locals map.
        // Cannot add them directly because localVariableSensitiveInstructions is in an unpredictable order which would
        // lead to randomness in the output.
        Map<Integer, @Nullable Type> localVariablesToAdd = new TreeMap<>();

        localVariableSensitiveInstructions.forEach((asmInsn, chasmInsn) -> {
            int opcode = asmInsn.getOpcode();
            int varIndex = getVar(asmInsn);
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

                            localVariablesToAdd.put(sourceStore, value.getType());
                        }
                    }
                }
            }
            if (localLabel != null) {
                chasmInsn.getEntries().put(NodeConstants.VAR, new StringNode(localLabel));
            }
        });

        localVariablesToAdd.forEach((id, type) -> {
            MapNode local = Ast.emptyMap();
            if (type != null) {
                local.getEntries().put(NodeConstants.TYPE, Ast.literal(type.getDescriptor()));
            }
            this.locals.getEntries().put("V" + id, local);
        });
    }

    private int[] computeEquivalentStores(MethodNode method, Frame<LocalValue>[] frames) {
        // This is initialized to zero. Zero means there is no other equivalent store.
        // We rely on the fact that the first instruction cannot be a store, since it would pop a non-existent value off
        // the stack.
        int[] equivalentStores = new int[frames.length];

        for (AbstractInsnNode insn : method.instructions) {
            int opcode = insn.getOpcode();
            int index = method.instructions.indexOf(insn);
            if (opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE) {
                if (index + 1 < frames.length) {
                    Frame<LocalValue> frame = frames[index + 1];
                    if (frame != null) {
                        int[] sourceStoresHere = frame.getLocal(getVar(insn)).getSourceStores();
                        findEquivalentSourceStores(equivalentStores, index, sourceStoresHere);
                    }
                }
            } else if (opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD || opcode == Opcodes.RET) {
                Frame<LocalValue> frame = frames[index];
                if (frame != null) {
                    int[] sourceStoresHere = frame.getLocal(getVar(insn)).getSourceStores();
                    findEquivalentSourceStores(equivalentStores, index, sourceStoresHere);
                }
            } else if (opcode == Opcodes.IINC) {
                Frame<LocalValue> frame = frames[index];
                if (frame != null) {
                    int[] sourceStoresHere = frame.getLocal(getVar(insn)).getSourceStores();
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

    private Collection<String> findChasmLocalsForSourceLocal(int slot, LabelNode start, LabelNode end) {
        if (slot == 0 && !isStatic) {
            return Collections.singletonList("this");
        }
        int paramSlot = isStatic ? 0 : 1;
        int paramIndex = 0;
        for (Type argType : Type.getArgumentTypes(this.descriptor)) {
            if (slot == paramSlot) {
                return Collections.singletonList("P" + paramIndex);
            }
            paramIndex++;
            paramSlot += argType.getSize();
        }

        Set<String> result = new LinkedHashSet<>();
        for (AbstractInsnNode insn = start; insn != null && insn != end; insn = insn.getNext()) {
            MapNode chasmInsn = localVariableSensitiveInstructions.get(insn);
            if (chasmInsn == null) {
                continue;
            }
            if (getVar(insn) == slot) {
                result.add(NodeUtils.getAsString(chasmInsn, NodeConstants.VAR));
            }
        }
        return result;
    }

    private void handleSourceLocals() {
        MethodNode methodNode = (MethodNode) this.mv;
        if (methodNode.instructions == null) {
            return;
        }

        if (methodNode.localVariables != null) {
            for (LocalVariableNode localVariable : methodNode.localVariables) {
                handleSourceLocal(localVariable);
            }
        }

        if (methodNode.invisibleLocalVariableAnnotations != null) {
            for (LocalVariableAnnotationNode annotation : methodNode.invisibleLocalVariableAnnotations) {
                handleLocalVariableAnnotation(annotation, false);
            }
        }
        if (methodNode.visibleLocalVariableAnnotations != null) {
            for (LocalVariableAnnotationNode annotation : methodNode.visibleLocalVariableAnnotations) {
                handleLocalVariableAnnotation(annotation, true);
            }
        }
    }

    private void handleSourceLocal(LocalVariableNode localVariable) {
        Collection<String> chasmLocals
                = findChasmLocalsForSourceLocal(localVariable.index, localVariable.start, localVariable.end);
        for (String chasmLocal : chasmLocals) {
            MapNode localNode = NodeUtils.getAsMap(this.locals, chasmLocal);
            if (localVariable.name != null) {
                localNode.getEntries().put(NodeConstants.SOURCE_NAME, Ast.literal(localVariable.name));
            }
            if (localVariable.desc != null) {
                localNode.getEntries().put(NodeConstants.SOURCE_DESCRIPTOR, Ast.literal(localVariable.desc));
            }
            if (localVariable.signature != null) {
                localNode.getEntries().put(NodeConstants.SIGNATURE, Ast.literal(localVariable.signature));
            }
        }
    }

    private void handleLocalVariableAnnotation(LocalVariableAnnotationNode annotation, boolean visible) {
        for (int i = 0; i < annotation.start.size(); i++) {
            int slot = annotation.index.get(i);
            LabelNode start = annotation.start.get(i);
            LabelNode end = annotation.end.get(i);
            Collection<String> chasmLocals = findChasmLocalsForSourceLocal(slot, start, end);
            for (String chasmLocal : chasmLocals) {
                MapNode localNode = NodeUtils.getAsMap(this.locals, chasmLocal);
                ListNode annotations = NodeUtils.asList(
                        localNode.getEntries().computeIfAbsent(NodeConstants.ANNOTATIONS, k -> Ast.emptyList()));
                MapNode values = Ast.emptyMap();
                annotations.getEntries().add(Ast.map()
                        .put(NodeConstants.VISIBLE, visible)
                        .put(NodeConstants.TYPE_REF, annotation.typeRef)
                        .put(NodeConstants.TYPE_PATH, annotation.typePath.toString())
                        .put(NodeConstants.DESCRIPTOR, annotation.desc)
                        .put(NodeConstants.VALUES, values)
                        .build());
                annotation.accept(new ChasmAnnotationVisitor(Opcodes.ASM9, values));
            }
        }
    }

    private static int getVar(AbstractInsnNode insn) {
        return insn instanceof IincInsnNode ? ((IincInsnNode) insn).var : ((VarInsnNode) insn).var;
    }
}
