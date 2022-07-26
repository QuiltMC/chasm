package org.quiltmc.chasm.internal.tree.reader;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class MethodNodeReader {
    private final MapNode methodNode;

    public MethodNodeReader(MapNode methodNode) {
        this.methodNode = methodNode;
    }

    private static Object[] getArguments(ListNode argumentNode) {
        Object[] arguments = new Object[argumentNode.getEntries().size()];

        int i = 0;
        for (Node entry : argumentNode.getEntries()) {
            arguments[i++] = NodeUtils.fromValueNode(entry);
        }

        return arguments;
    }

    private static Label obtainLabel(Map<String, Label> labelMap, String labelName) {
        return labelMap.computeIfAbsent(labelName, unusedLabelName -> new Label());
    }

    private static void visitInstructions(
            boolean isStatic,
            ListNode params,
            MethodVisitor methodVisitor,
            MapNode codeNode,
            Map<String, Label> labelMap
    ) {
        Map<String, Integer> remappedLocalIndexes = new HashMap<>();
        int[] nextLocalIndex = new int[] {0};
        if (!isStatic) {
            remappedLocalIndexes.put("this", nextLocalIndex[0]++);
        }
        for (Node paramNode : params.getEntries()) {
            MapNode param = NodeUtils.asMap(paramNode);
            remappedLocalIndexes.put(NodeUtils.getAsString(param, NodeConstants.NAME), nextLocalIndex[0]);
            Type type = Type.getType(NodeUtils.getAsString(param, NodeConstants.TYPE));
            nextLocalIndex[0] += type.getSize();
        }

        for (Node rawInstruction : NodeUtils.getAsList(codeNode, NodeConstants.INSTRUCTIONS).getEntries()) {
            MapNode instruction = NodeUtils.asMap(rawInstruction);

            // visitLabel
            if (instruction.getEntries().containsKey(NodeConstants.LABEL)) {
                String label = NodeUtils.getAsString(instruction, NodeConstants.LABEL);
                methodVisitor.visitLabel(obtainLabel(labelMap, label));
                continue;
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
                    int localIndex = getLocalIndex(nextLocalIndex, remappedLocalIndexes, varName, size);
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
                    Object[] args = new Object[arguments.getEntries().size()];

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
                    Label label = obtainLabel(labelMap, labelString);
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
                    int varIndex = getLocalIndex(nextLocalIndex, remappedLocalIndexes, varName, 1);
                    int increment = NodeUtils.getAsInt(instruction, NodeConstants.INCREMENT);
                    methodVisitor.visitIincInsn(varIndex, increment);
                    break;
                }
                case Opcodes.TABLESWITCH:
                case Opcodes.LOOKUPSWITCH: {
                    // visitTableSwitchInsn / visitLookupSwitchInsn
                    String defaultString = NodeUtils.getAsString(instruction, NodeConstants.DEFAULT);
                    Label dflt = obtainLabel(labelMap, defaultString);
                    ListNode cases = NodeUtils.getAsList(instruction, NodeConstants.CASES);
                    int[] keys = new int[cases.getEntries().size()];
                    Label[] labels = new Label[cases.getEntries().size()];
                    for (int i = 0; i < cases.getEntries().size(); i++) {
                        MapNode caseNode = NodeUtils.asMap(cases.getEntries().get(i));
                        keys[i] = NodeUtils.getAsInt(caseNode, NodeConstants.KEY);
                        String caseLabelString = NodeUtils.getAsString(caseNode, NodeConstants.LABEL);
                        labels[i] = obtainLabel(labelMap, caseLabelString);
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

    private static int getLocalIndex(
            int[] nextLocalIndex,
            Map<String, Integer> remappedLocalIndexes,
            String varName,
            int size
    ) {
        // TODO: smart merging of local variable indexes
        return remappedLocalIndexes.computeIfAbsent(varName, k -> {
            int index = nextLocalIndex[0];
            nextLocalIndex[0] += size;
            return index;
        });
    }

    private static void visitTryCatchBlocks(MethodVisitor methodVisitor, MapNode codeNode,
                                            Map<String, Label> labelMap) {
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

    private void visitLocalVariables(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        ListNode codeLocalsNode = NodeUtils.getAsList(codeNode, NodeConstants.SOURCE_LOCALS);
        if (codeLocalsNode == null) {
            return;
        }

        for (Node n : codeLocalsNode.getEntries()) {
            MapNode localNode = NodeUtils.asMap(n);
            String localName = NodeUtils.getAsString(localNode, NodeConstants.NAME);
            String localDesc = NodeUtils.getAsString(localNode, NodeConstants.DESCRIPTOR);
            String localSignature = NodeUtils.getAsString(localNode, NodeConstants.SIGNATURE);

            String start = NodeUtils.getAsString(localNode, NodeConstants.START);
            String end = NodeUtils.getAsString(localNode, NodeConstants.END);
            int index = NodeUtils.getAsInt(localNode, NodeConstants.INDEX);

            Label startLabel = labelMap.get(start);
            Label endLabel = labelMap.get(end);

            methodVisitor.visitLocalVariable(localName, localDesc, localSignature, startLabel, endLabel, index);

            // visitLocalVariableAnnotation
            // TODO
        }
    }

    private void visitLineNumbers(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
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
        methodVisitor.visitAnnotableParameterCount(parameters.getEntries().size(), true);
        methodVisitor.visitAnnotableParameterCount(parameters.getEntries().size(), false);

        // visitParameterAnnotation
        for (int i = 0; i < parameters.getEntries().size(); i++) {
            int index = i;
            MapNode parameterNode = NodeUtils.asMap(parameters.getEntries().get(i));
            ListNode annotations = NodeUtils.getAsList(parameterNode, NodeConstants.ANNOTATIONS);
            if (annotations != null) {
                for (Node node : annotations.getEntries()) {
                    MapNode annotationNode = NodeUtils.asMap(node);

                    AnnotationNodeReader reader = new AnnotationNodeReader(node);
                    reader.accept((d, v) -> methodVisitor.visitParameterAnnotation(index, d, v), null);
                }
            }
        }
    }

    private void visitParameters(MethodVisitor methodVisitor) {
        ListNode methodParametersNode = NodeUtils.getAsList(methodNode, NodeConstants.PARAMETERS);
        for (Node n : methodParametersNode.getEntries()) {
            MapNode parameterNode = NodeUtils.asMap(n);
            String name = NodeUtils.getAsString(parameterNode, NodeConstants.NAME);
            Long access = NodeUtils.getAsLong(parameterNode, NodeConstants.ACCESS);
            methodVisitor.visitParameter(name, access == null ? 0 : access.intValue());
        }
    }

    public void visitMethod(ClassVisitor visitor) {
        int access = NodeUtils.getAsInt(methodNode, NodeConstants.ACCESS);
        String name = NodeUtils.getAsString(methodNode, NodeConstants.NAME);

        Type returnType = Type.getType(NodeUtils.getAsString(methodNode, NodeConstants.RETURN_TYPE));
        ListNode parameters = NodeUtils.getAsList(methodNode, NodeConstants.PARAMETERS);
        Type[] parameterTypes = new Type[parameters.getEntries().size()];

        int i = 0;
        for (Node entry : parameters.getEntries()) {
            parameterTypes[i++] = Type.getType(NodeUtils.getAsString(entry, NodeConstants.TYPE));
        }

        String descriptor = Type.getMethodDescriptor(returnType, parameterTypes);
        String signature = NodeUtils.getAsString(methodNode, NodeConstants.SIGNATURE);

        ListNode exceptionsNode = NodeUtils.getAsList(methodNode, NodeConstants.EXCEPTIONS);
        String[] exceptions = exceptionsNode == null ? new String[0] :
                exceptionsNode.getEntries().stream().map(NodeUtils::asString).toArray(String[]::new);

        MethodVisitor methodVisitor = visitor.visitMethod(access, name, descriptor, signature, exceptions);

        // visitParameter
        visitParameters(methodVisitor);

        // visitAnnotableParameterCount/visitParameterAnnotation
        visitParameterAnnotations(methodVisitor);

        // visitAnnotationDefault
        visitAnnotationDefault(methodVisitor);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(methodVisitor);

        // visitCod
        if (methodNode.getEntries().containsKey(NodeConstants.CODE)) {
            MapNode codeNode = NodeUtils.getAsMap(methodNode, NodeConstants.CODE);
            Map<String, Label> labelMap = new HashMap<>();

            // visitFrame
            // Don't care

            // Instructions
            visitInstructions((access & Opcodes.ACC_STATIC) != 0, parameters, methodVisitor, codeNode, labelMap);

            // visitTryCatchBlock
            visitTryCatchBlocks(methodVisitor, codeNode, labelMap);

            // visitLocalVariable
            visitLocalVariables(methodVisitor, codeNode, labelMap);

            // visitLineNumber
            visitLineNumbers(methodVisitor, codeNode, labelMap);

            // visitMaxs
            methodVisitor.visitMaxs(0, 0);
        }

        // visitEnd
        methodVisitor.visitEnd();
    }
}
