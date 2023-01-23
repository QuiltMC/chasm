package org.quiltmc.chasm.internal.tree.reader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableAnnotationNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.internal.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MethodNodeReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodNodeReader.class);

    private final String className;
    private final MapNode methodNode;
    private MethodNode outputMethodNode;
    private final Map<String, Label> labelMap = new HashMap<>();
    private final Map<String, Integer> localVariableIndexes = new HashMap<>();
    private int nextLocalIndex = 0;

    public MethodNodeReader(String className, MapNode methodNode) {
        this.className = className;
        this.methodNode = methodNode;
    }

    private Label obtainLabel(String labelName) {
        return this.labelMap.computeIfAbsent(labelName, unusedLabelName -> new Label());
    }

    private void visitInstructions(
            boolean isStatic,
            ListNode params,
            MethodVisitor methodVisitor,
            MapNode codeNode
    ) {
        if (!isStatic) {
            this.localVariableIndexes.put("this", nextLocalIndex++);
        }
        for (Node paramNode : params.getEntries()) {
            String paramName = NodeUtils.asString(paramNode);
            MapNode param = NodeUtils.getAsMap(NodeUtils.getAsMap(methodNode, NodeConstants.LOCALS), paramName);
            this.localVariableIndexes.put(paramName, nextLocalIndex);
            Type type = Type.getType(NodeUtils.getAsString(param, NodeConstants.TYPE));
            nextLocalIndex += type.getSize();
        }

        ListNode instructions = NodeUtils.getAsList(codeNode, NodeConstants.INSTRUCTIONS);

        // insert a label at the start of the instructions if there isn't one already
        MapNode firstInstruction = NodeUtils.asMap(instructions.get(0));
        if (!firstInstruction.getEntries().containsKey(NodeConstants.LABEL)) {
            methodVisitor.visitLabel(new Label());
        }

        for (int index = 0; index < instructions.size(); index++) {
            Node rawInstruction = instructions.get(index);
            MapNode instruction = NodeUtils.asMap(rawInstruction);

            // visitLabel
            if (instruction.getEntries().containsKey(NodeConstants.LABEL)) {
                String labelName = NodeUtils.getAsString(instruction, NodeConstants.LABEL);
                Label label = obtainLabel(labelName);
                methodVisitor.visitLabel(label);
                continue;
            }

            // insert a label just before the end of the instructions (before the return insn),
            // if there isn't one already
            if (index == instructions.size() - 1 && index != 0) {
                MapNode prevInstruction = NodeUtils.asMap(instructions.get(index - 1));
                if (!prevInstruction.getEntries().containsKey(NodeConstants.LABEL)) {
                    methodVisitor.visitLabel(new Label());
                }
            }

            // visit<...>Insn
            int opcode = NodeUtils.getAsInt(instruction, NodeConstants.OPCODE);
            switch (opcode) {
                case Opcodes.ACONST_NULL:
                case Opcodes.ICONST_M1:
                case Opcodes.ICONST_0:
                case Opcodes.ICONST_1:
                case Opcodes.ICONST_2:
                case Opcodes.ICONST_3:
                case Opcodes.ICONST_4:
                case Opcodes.ICONST_5:
                case Opcodes.LCONST_0:
                case Opcodes.LCONST_1:
                case Opcodes.FCONST_0:
                case Opcodes.FCONST_1:
                case Opcodes.FCONST_2:
                case Opcodes.DCONST_0:
                case Opcodes.DCONST_1:
                case Opcodes.IALOAD:
                case Opcodes.LALOAD:
                case Opcodes.FALOAD:
                case Opcodes.DALOAD:
                case Opcodes.AALOAD:
                case Opcodes.BALOAD:
                case Opcodes.CALOAD:
                case Opcodes.SALOAD:
                case Opcodes.IASTORE:
                case Opcodes.LASTORE:
                case Opcodes.FASTORE:
                case Opcodes.DASTORE:
                case Opcodes.AASTORE:
                case Opcodes.BASTORE:
                case Opcodes.CASTORE:
                case Opcodes.SASTORE:
                case Opcodes.POP:
                case Opcodes.POP2:
                case Opcodes.DUP:
                case Opcodes.DUP_X1:
                case Opcodes.DUP_X2:
                case Opcodes.DUP2:
                case Opcodes.DUP2_X1:
                case Opcodes.DUP2_X2:
                case Opcodes.SWAP:
                case Opcodes.IADD:
                case Opcodes.LADD:
                case Opcodes.FADD:
                case Opcodes.DADD:
                case Opcodes.ISUB:
                case Opcodes.LSUB:
                case Opcodes.FSUB:
                case Opcodes.DSUB:
                case Opcodes.IMUL:
                case Opcodes.LMUL:
                case Opcodes.FMUL:
                case Opcodes.DMUL:
                case Opcodes.IDIV:
                case Opcodes.LDIV:
                case Opcodes.FDIV:
                case Opcodes.DDIV:
                case Opcodes.IREM:
                case Opcodes.LREM:
                case Opcodes.FREM:
                case Opcodes.DREM:
                case Opcodes.INEG:
                case Opcodes.LNEG:
                case Opcodes.FNEG:
                case Opcodes.DNEG:
                case Opcodes.ISHL:
                case Opcodes.LSHL:
                case Opcodes.ISHR:
                case Opcodes.LSHR:
                case Opcodes.IUSHR:
                case Opcodes.LUSHR:
                case Opcodes.IAND:
                case Opcodes.LAND:
                case Opcodes.IOR:
                case Opcodes.LOR:
                case Opcodes.IXOR:
                case Opcodes.LXOR:
                case Opcodes.I2L:
                case Opcodes.I2F:
                case Opcodes.I2D:
                case Opcodes.L2I:
                case Opcodes.L2F:
                case Opcodes.L2D:
                case Opcodes.F2I:
                case Opcodes.F2L:
                case Opcodes.F2D:
                case Opcodes.D2I:
                case Opcodes.D2L:
                case Opcodes.D2F:
                case Opcodes.I2B:
                case Opcodes.I2C:
                case Opcodes.I2S:
                case Opcodes.LCMP:
                case Opcodes.FCMPL:
                case Opcodes.FCMPG:
                case Opcodes.DCMPL:
                case Opcodes.DCMPG:
                case Opcodes.IRETURN:
                case Opcodes.LRETURN:
                case Opcodes.FRETURN:
                case Opcodes.DRETURN:
                case Opcodes.ARETURN:
                case Opcodes.RETURN:
                case Opcodes.ARRAYLENGTH:
                case Opcodes.ATHROW:
                case Opcodes.MONITORENTER:
                case Opcodes.MONITOREXIT: {
                    // visitInsn
                    methodVisitor.visitInsn(opcode);
                    break;
                }
                case Opcodes.BIPUSH:
                case Opcodes.SIPUSH:
                case Opcodes.NEWARRAY: {
                    // visitIntInsn
                    int operand = NodeUtils.getAsInt(instruction, NodeConstants.OPERAND);
                    methodVisitor.visitIntInsn(opcode, operand);
                    break;
                }
                case Opcodes.ILOAD:
                case Opcodes.LLOAD:
                case Opcodes.FLOAD:
                case Opcodes.DLOAD:
                case Opcodes.ALOAD:
                case Opcodes.ISTORE:
                case Opcodes.LSTORE:
                case Opcodes.FSTORE:
                case Opcodes.DSTORE:
                case Opcodes.ASTORE:
                case Opcodes.RET: {
                    // visitVarInsn
                    String varName = NodeUtils.getAsString(instruction, NodeConstants.VAR);
                    int size = opcode == Opcodes.LLOAD
                            || opcode == Opcodes.DLOAD
                            || opcode == Opcodes.LSTORE
                            || opcode == Opcodes.DSTORE ? 2 : 1;
                    int localIndex = getLocalIndex(varName, size);
                    methodVisitor.visitVarInsn(opcode, localIndex);
                    break;
                }
                case Opcodes.NEW:
                case Opcodes.ANEWARRAY:
                case Opcodes.CHECKCAST:
                case Opcodes.INSTANCEOF: {
                    // visitTypeInsn
                    String type = NodeUtils.getAsString(instruction, NodeConstants.TYPE);
                    methodVisitor.visitTypeInsn(opcode, type);
                    break;
                }
                case Opcodes.GETSTATIC:
                case Opcodes.PUTSTATIC:
                case Opcodes.GETFIELD:
                case Opcodes.PUTFIELD: {
                    // visitFieldInsn
                    String owner = NodeUtils.getAsString(instruction, NodeConstants.OWNER);
                    String name = NodeUtils.getAsString(instruction, NodeConstants.NAME);
                    String descriptor = NodeUtils.getAsString(instruction, NodeConstants.DESCRIPTOR);
                    methodVisitor.visitFieldInsn(opcode, owner, name, descriptor);
                    break;
                }
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEINTERFACE: {
                    // visitMethodInsns
                    String owner = NodeUtils.getAsString(instruction, NodeConstants.OWNER);
                    String name = NodeUtils.getAsString(instruction, NodeConstants.NAME);
                    String descriptor = NodeUtils.getAsString(instruction, NodeConstants.DESCRIPTOR);
                    Boolean isInterface = NodeUtils.getAsBoolean(instruction, NodeConstants.IS_INTERFACE);
                    methodVisitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                    break;
                }
                case Opcodes.INVOKEDYNAMIC: {
                    // visitInvokeDynamicInsn
                    String name = NodeUtils.getAsString(instruction, NodeConstants.NAME);
                    String descriptor = NodeUtils.getAsString(instruction, NodeConstants.DESCRIPTOR);
                    Handle handle = NodeUtils.asHandle(NodeUtils.get(instruction, NodeConstants.HANDLE));
                    ListNode arguments = NodeUtils.getAsList(instruction, NodeConstants.ARGUMENTS);
                    Object[] args = new Object[arguments.size()];

                    int i = 0;
                    for (Node entry : arguments.getEntries()) {
                        args[i++] = NodeUtils.fromValueNode(entry);
                    }

                    methodVisitor.visitInvokeDynamicInsn(name, descriptor, handle, args);
                    break;
                }
                case Opcodes.IFEQ:
                case Opcodes.IFNE:
                case Opcodes.IFLT:
                case Opcodes.IFGE:
                case Opcodes.IFGT:
                case Opcodes.IFLE:
                case Opcodes.IF_ICMPEQ:
                case Opcodes.IF_ICMPNE:
                case Opcodes.IF_ICMPLT:
                case Opcodes.IF_ICMPGE:
                case Opcodes.IF_ICMPGT:
                case Opcodes.IF_ICMPLE:
                case Opcodes.IF_ACMPEQ:
                case Opcodes.IF_ACMPNE:
                case Opcodes.GOTO:
                case Opcodes.JSR:
                case Opcodes.IFNULL:
                case Opcodes.IFNONNULL: {
                    // visitJumpInsns
                    String labelString = NodeUtils.getAsString(instruction, NodeConstants.TARGET);
                    Label label = obtainLabel(labelString);
                    methodVisitor.visitJumpInsn(opcode, label);
                    break;
                }
                case Opcodes.LDC: {
                    // visitLdcInsn
                    Object value = NodeUtils.fromValueNode(NodeUtils.get(instruction, NodeConstants.VALUE));
                    methodVisitor.visitLdcInsn(value);
                    break;
                }
                case Opcodes.IINC: {
                    // visitIincInsn
                    String varName = NodeUtils.getAsString(instruction, NodeConstants.VAR);
                    int varIndex = getLocalIndex(varName, 1);
                    int increment = NodeUtils.getAsInt(instruction, NodeConstants.INCREMENT);
                    methodVisitor.visitIincInsn(varIndex, increment);
                    break;
                }
                case Opcodes.TABLESWITCH:
                case Opcodes.LOOKUPSWITCH: {
                    // visitTableSwitchInsn / visitLookupSwitchInsn
                    String defaultString = NodeUtils.getAsString(instruction, NodeConstants.DEFAULT);
                    Label dflt = obtainLabel(defaultString);
                    ListNode cases = NodeUtils.getAsList(instruction, NodeConstants.CASES);
                    int[] keys = new int[cases.size()];
                    Label[] labels = new Label[cases.size()];
                    for (int i = 0; i < cases.size(); i++) {
                        MapNode caseNode = NodeUtils.asMap(cases.get(i));
                        keys[i] = NodeUtils.getAsInt(caseNode, NodeConstants.KEY);
                        String caseLabelString = NodeUtils.getAsString(caseNode, NodeConstants.LABEL);
                        labels[i] = obtainLabel(caseLabelString);
                    }

                    if (opcode == Opcodes.LOOKUPSWITCH) {
                        methodVisitor.visitLookupSwitchInsn(dflt, keys, labels);
                    } else {
                        // Check if switch can still be a table switch
                        boolean canBeTable = true;
                        for (int i = 0; i < keys.length; i++) {
                            if (keys[i] != keys[0] + i) {
                                canBeTable = false;
                                break;
                            }
                        }

                        if (canBeTable) {
                            methodVisitor
                                    .visitTableSwitchInsn(keys[0], keys[0] + keys.length - 1, dflt, labels);
                        } else {
                            methodVisitor.visitLookupSwitchInsn(dflt, keys, labels);
                        }
                    }
                    break;
                }
                case Opcodes.MULTIANEWARRAY: {
                    // visitMultiANewArrayInsn
                    String descriptor = NodeUtils.getAsString(instruction, NodeConstants.DESCRIPTOR);
                    int dimensions = NodeUtils.getAsInt(instruction, NodeConstants.DIMENSIONS);
                    methodVisitor.visitMultiANewArrayInsn(descriptor, dimensions);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown instruction opcode");
            }

            // visitInsnAnnotation
            ListNode annotations = NodeUtils.getAsList(instruction, NodeConstants.ANNOTATIONS);
            if (annotations != null) {
                for (Node n : annotations.getEntries()) {
                    new AnnotationNodeReader(n).accept(null, methodVisitor::visitInsnAnnotation);
                }
            }
        }
    }

    private int getLocalIndex(
            String varName,
            int size
    ) {
        // TODO: Smart merging of local variable indexes. Remember that source locals exist too!
        return this.localVariableIndexes.computeIfAbsent(varName, k -> {
            int index = nextLocalIndex;
            nextLocalIndex += size;
            return index;
        });
    }

    private void visitTryCatchBlocks(MethodVisitor methodVisitor, MapNode codeNode) {
        ListNode tryCatchBlocksListNode = NodeUtils.getAsList(codeNode, NodeConstants.TRY_CATCH_BLOCKS);
        if (tryCatchBlocksListNode == null) {
            return;
        }
        for (Node n : tryCatchBlocksListNode.getEntries()) {
            MapNode tryCatchBlock = NodeUtils.asMap(n);

            String start = NodeUtils.getAsString(tryCatchBlock, NodeConstants.START);
            String end = NodeUtils.getAsString(tryCatchBlock, NodeConstants.END);
            String handler = NodeUtils.getAsString(tryCatchBlock, NodeConstants.HANDLER);
            String type = NodeUtils.getAsString(tryCatchBlock, NodeConstants.TYPE);

            Label startLabel = labelMap.computeIfAbsent(start, s -> new Label());
            Label endLabel = labelMap.computeIfAbsent(end, s -> new Label());
            Label handlerLabel = labelMap.computeIfAbsent(handler, s -> new Label());

            methodVisitor.visitTryCatchBlock(startLabel, endLabel, handlerLabel, type);

            // visitTryCatchBlockAnnotations
            ListNode annotations = NodeUtils.getAsList(tryCatchBlock, NodeConstants.ANNOTATIONS);
            for (Node annotation : annotations.getEntries()) {
                AnnotationNodeReader reader = new AnnotationNodeReader(annotation);
                reader.accept(null, methodVisitor::visitTypeAnnotation);
            }
        }
    }

    private void addLocalVariableDebugInfo() {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
        Frame<BasicValue>[] frames;
        try {
            frames = analyzer.analyzeAndComputeMaxs(this.className, this.outputMethodNode);
        } catch (AnalyzerException e) {
            LOGGER.error("Failed to analyze method {}.{}{}, local variable debug info will not be present",
                    className, outputMethodNode.name, outputMethodNode.desc);
            return;
        }
        @SuppressWarnings("unchecked")
        List<Integer>[] readabilityStarts = new List[this.outputMethodNode.maxLocals];
        Arrays.setAll(readabilityStarts, i -> new ArrayList<>());
        @SuppressWarnings("unchecked")
        List<Integer>[] readabilityEnds = new List[this.outputMethodNode.maxLocals];
        Arrays.setAll(readabilityEnds, i -> new ArrayList<>());
        for (int insnIndex = 0; insnIndex < frames.length; insnIndex++) {
            for (int localIndex = 0; localIndex < this.outputMethodNode.maxLocals; localIndex++) {
                boolean isReadable = frames[insnIndex] != null
                        && !BasicValue.UNINITIALIZED_VALUE.equals(frames[insnIndex].getLocal(localIndex));
                boolean wasReadable = insnIndex != 0 && frames[insnIndex - 1] != null
                        && !BasicValue.UNINITIALIZED_VALUE.equals(frames[insnIndex - 1].getLocal(localIndex));
                if (isReadable && !wasReadable) {
                    readabilityStarts[localIndex].add(insnIndex);
                } else if (!isReadable && wasReadable) {
                    readabilityEnds[localIndex].add(insnIndex);
                }
            }
        }
        for (int localIndex = 0; localIndex < this.outputMethodNode.maxLocals; localIndex++) {
            if (readabilityEnds[localIndex].size() == readabilityStarts[localIndex].size() - 1) {
                readabilityEnds[localIndex].add(frames.length - 1);
            }
            Assert.check(readabilityEnds[localIndex].size() == readabilityStarts[localIndex].size());
        }

        MapNode locals = NodeUtils.getAsMap(this.methodNode, NodeConstants.LOCALS);
        locals.getEntries().forEach((name, rawLocal) -> {
            MapNode local = NodeUtils.asMap(rawLocal);
            Integer index = this.localVariableIndexes.get(name);
            if (index == null) {
                return;
            }
            String sourceName = NodeUtils.getAsString(local, NodeConstants.SOURCE_NAME);
            String sourceDesc = NodeUtils.getAsString(local, NodeConstants.SOURCE_DESCRIPTOR);
            String signature = NodeUtils.getAsString(local, NodeConstants.SIGNATURE);
            if (sourceName != null || sourceDesc != null || signature != null) {
                for (int scopeIndex = 0; scopeIndex < readabilityStarts[index].size(); scopeIndex++) {
                    LabelNode startLabel = findClosestLabel(readabilityStarts[index].get(scopeIndex), true);
                    LabelNode endLabel = findClosestLabel(readabilityEnds[index].get(scopeIndex), false);
                    if (outputMethodNode.localVariables == null) {
                        outputMethodNode.localVariables = new ArrayList<>();
                    }
                    outputMethodNode.localVariables.add(
                            new LocalVariableNode(sourceName, sourceDesc, signature, startLabel, endLabel, index));
                }
            }

            ListNode annotations = NodeUtils.getAsList(local, NodeConstants.ANNOTATIONS);
            if (annotations != null) {
                for (Node rawAnnotation : annotations.getEntries()) {
                    MapNode annotation = NodeUtils.asMap(rawAnnotation);
                    final int typeRef = NodeUtils.getAsInt(annotation, NodeConstants.TYPE_REF);
                    final String typePath = NodeUtils.getAsString(annotation, NodeConstants.TYPE_PATH);
                    final String descriptor = NodeUtils.getAsString(annotation, NodeConstants.DESCRIPTOR);
                    final boolean visible = NodeUtils.getAsBoolean(annotation, NodeConstants.VISIBLE);

                    LabelNode[] localStarts = new LabelNode[readabilityStarts[index].size()];
                    Arrays.setAll(localStarts, i -> findClosestLabel(readabilityStarts[index].get(i), true));
                    LabelNode[] localEnds = new LabelNode[localStarts.length];
                    Arrays.setAll(localEnds, i -> findClosestLabel(readabilityEnds[index].get(i), false));
                    int[] indexes = new int[localStarts.length];
                    Arrays.fill(indexes, index);

                    LocalVariableAnnotationNode node = new LocalVariableAnnotationNode(
                            typeRef, TypePath.fromString(typePath), localStarts, localEnds, indexes, descriptor);
                    AnnotationNodeReader.visitValues(node, NodeUtils.getAsMap(annotation, NodeConstants.VALUES));
                    if (visible) {
                        if (outputMethodNode.visibleLocalVariableAnnotations == null) {
                            outputMethodNode.visibleLocalVariableAnnotations = new ArrayList<>();
                        }
                        outputMethodNode.visibleLocalVariableAnnotations.add(node);
                    } else {
                        if (outputMethodNode.invisibleLocalVariableAnnotations == null) {
                            outputMethodNode.invisibleLocalVariableAnnotations = new ArrayList<>();
                        }
                        outputMethodNode.invisibleLocalVariableAnnotations.add(node);
                    }
                }
            }
        });
    }

    @Nullable
    private LabelNode findClosestLabel(int insnIndex, boolean preferReverse) {
        for (int i = 0; i < 2; i++) {
            for (AbstractInsnNode insn = this.outputMethodNode.instructions.get(insnIndex);
                    insn != null;
                    insn = preferReverse ? insn.getPrevious() : insn.getNext()) {
                if (insn instanceof LabelNode) {
                    return (LabelNode) insn;
                }
            }
            preferReverse = !preferReverse;
        }
        return null;
    }

    private void visitLineNumbers(MethodVisitor methodVisitor, MapNode codeNode) {
        ListNode lineNumbers = NodeUtils.getAsList(codeNode, NodeConstants.LINE_NUMBERS);
        if (lineNumbers != null) {
            for (Node n : lineNumbers.getEntries()) {
                MapNode lineNumber = NodeUtils.asMap(n);
                int line = NodeUtils.getAsInt(lineNumber, NodeConstants.LINE);
                String labelName = NodeUtils.getAsString(lineNumber, NodeConstants.LABEL);
                Label label = labelMap.get(labelName);
                if (label != null) {
                    methodVisitor.visitLineNumber(line, label);
                }
            }
        }
    }

    private void visitAnnotations(MethodVisitor methodVisitor) {
        ListNode methodAnnotationsNode = NodeUtils.getAsList(methodNode, NodeConstants.ANNOTATIONS);
        if (methodAnnotationsNode != null) {
            for (Node n : methodAnnotationsNode.getEntries()) {
                AnnotationNodeReader reader = new AnnotationNodeReader(n);
                reader.accept(methodVisitor::visitAnnotation, methodVisitor::visitTypeAnnotation);
            }
        }
    }

    private void visitAnnotationDefault(MethodVisitor methodVisitor) {
        Node annotationDefault = NodeUtils.get(methodNode, NodeConstants.ANNOTATION_DEFAULT);
        if (annotationDefault != null) {
            AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
            AnnotationNodeReader.visitValues(annotationVisitor, annotationDefault);
        }
    }

    private void visitParameterAnnotations(MethodVisitor methodVisitor) {
        ListNode parameters = NodeUtils.getAsList(methodNode, NodeConstants.PARAMETERS);

        // visitAnnotableParameterCount
        // We make all parameters annotable
        methodVisitor.visitAnnotableParameterCount(parameters.size(), true);
        methodVisitor.visitAnnotableParameterCount(parameters.size(), false);

        // visitParameterAnnotation
        for (int i = 0; i < parameters.size(); i++) {
            int index = i;
            String paramName = NodeUtils.asString(parameters.get(i));
            MapNode parameterNode = NodeUtils.getAsMap(NodeUtils.getAsMap(methodNode, NodeConstants.LOCALS), paramName);
            ListNode annotations = NodeUtils.getAsList(parameterNode, NodeConstants.ANNOTATIONS);
            if (annotations != null) {
                for (Node node : annotations.getEntries()) {
                    AnnotationNodeReader reader = new AnnotationNodeReader(node);
                    reader.accept((d, v) -> methodVisitor.visitParameterAnnotation(index, d, v), null);
                }
            }
        }
    }

    private void visitParameters(MethodVisitor methodVisitor) {
        ListNode methodParametersNode = NodeUtils.getAsList(methodNode, NodeConstants.PARAMETERS);
        for (Node n : methodParametersNode.getEntries()) {
            String paramName = NodeUtils.asString(n);
            MapNode parameterNode = NodeUtils.getAsMap(NodeUtils.getAsMap(methodNode, NodeConstants.LOCALS), paramName);
            String name = NodeUtils.getAsString(parameterNode, NodeConstants.SOURCE_NAME);
            Long access = NodeUtils.getAsLong(parameterNode, NodeConstants.ACCESS);
            methodVisitor.visitParameter(name, access == null ? 0 : access.intValue());
        }
    }

    public void visitMethod(ClassVisitor visitor) {
        int access = NodeUtils.getAsInt(methodNode, NodeConstants.ACCESS);
        String name = NodeUtils.getAsString(methodNode, NodeConstants.NAME);

        Type returnType = Type.getType(NodeUtils.getAsString(methodNode, NodeConstants.RETURN_TYPE));
        ListNode parameters = NodeUtils.getAsList(methodNode, NodeConstants.PARAMETERS);
        Type[] parameterTypes = new Type[parameters.size()];

        int i = 0;
        for (Node entry : parameters.getEntries()) {
            String paramName = NodeUtils.asString(entry);
            MapNode locals = NodeUtils.getAsMap(methodNode, NodeConstants.LOCALS);
            MapNode param = NodeUtils.getAsMap(locals, paramName);
            parameterTypes[i++] = Type.getType(NodeUtils.getAsString(param, NodeConstants.TYPE));
        }

        String descriptor = Type.getMethodDescriptor(returnType, parameterTypes);
        String signature = NodeUtils.getAsString(methodNode, NodeConstants.SIGNATURE);

        ListNode exceptionsNode = NodeUtils.getAsList(methodNode, NodeConstants.EXCEPTIONS);
        String[] exceptions = exceptionsNode == null ? new String[0] :
                exceptionsNode.getEntries().stream().map(NodeUtils::asString).toArray(String[]::new);

        outputMethodNode = new MethodNode(access, name, descriptor, signature, exceptions);

        // visitParameter
        visitParameters(outputMethodNode);

        // visitAnnotableParameterCount/visitParameterAnnotation
        visitParameterAnnotations(outputMethodNode);

        // visitAnnotationDefault
        visitAnnotationDefault(outputMethodNode);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(outputMethodNode);

        // visitCode
        if (methodNode.getEntries().containsKey(NodeConstants.CODE)) {
            MapNode codeNode = NodeUtils.getAsMap(methodNode, NodeConstants.CODE);

            // visitFrame
            // Don't care

            // Instructions
            visitInstructions((access & Opcodes.ACC_STATIC) != 0, parameters, outputMethodNode, codeNode);

            // visitTryCatchBlock
            visitTryCatchBlocks(outputMethodNode, codeNode);

            // visitLineNumber
            visitLineNumbers(outputMethodNode, codeNode);

            // visitMaxs
            outputMethodNode.visitMaxs(0, 0);
        }

        // visitEnd
        outputMethodNode.visitEnd();

        addLocalVariableDebugInfo();

        outputMethodNode.accept(visitor);
    }
}
