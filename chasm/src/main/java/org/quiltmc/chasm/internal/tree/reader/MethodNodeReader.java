package org.quiltmc.chasm.internal.tree.reader;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class MethodNodeReader {
    private final MapNode methodNode;

    public MethodNodeReader(MapNode methodNode) {
        this.methodNode = methodNode;
    }

    private static Object[] getArguments(ListNode argumentNode) {
        Object[] arguments = new Object[argumentNode.size()];
        for (int i = 0; i < arguments.length; i++) {
            Node argNode = argumentNode.get(i);
            if (argNode instanceof ValueNode) {
                arguments[i] = Node.asValue(argNode).getValue();
            } else if ((Node.asMap(argNode)).containsKey(NodeConstants.TAG)) {
                arguments[i] = getHandle(Node.asMap(argNode));
            } else {
                MapNode constDynamicNode = Node.asMap(argNode);
                String name = Node.asValue(constDynamicNode.get(NodeConstants.NAME)).getValueAsString();
                String descriptor = Node.asValue(constDynamicNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                Handle handle = getHandle(Node.asMap(constDynamicNode.get(NodeConstants.HANDLE)));
                Object[] args = getArguments(Node.asList(constDynamicNode.get(NodeConstants.ARGS)));
                arguments[i] = new ConstantDynamic(name, descriptor, handle, args);
            }
        }

        return arguments;
    }

    public static Handle getHandle(MapNode handleNode) {
        int tag = Node.asValue(handleNode.get(NodeConstants.TAG)).getValueAsInt();
        String owner = Node.asValue(handleNode.get(NodeConstants.OWNER)).getValueAsString();
        String name = Node.asValue(handleNode.get(NodeConstants.NAME)).getValueAsString();
        String descriptor = Node.asValue(handleNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();
        boolean isInterface = Node.asValue(handleNode.get(NodeConstants.IS_INTERFACE)).getValueAsBoolean();

        return new Handle(tag, owner, name, descriptor, isInterface);
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
        for (Node paramNode : params) {
            MapNode param = Node.asMap(paramNode);
            remappedLocalIndexes.put(Node.asValue(param.get(NodeConstants.NAME)).getValueAsString(), nextLocalIndex[0]);
            Type type = Node.asValue(param.get(NodeConstants.TYPE)).getValueAs(Type.class);
            nextLocalIndex[0] += type.getSize();
        }

        for (Node rawInstruction : Node.asList(codeNode.get(NodeConstants.INSTRUCTIONS))) {
            MapNode instruction = Node.asMap(rawInstruction);

            // visitLabel
            if (instruction.containsKey(NodeConstants.LABEL)) {
                String label = Node.asValue(instruction.get(NodeConstants.LABEL)).getValueAsString();
                methodVisitor.visitLabel(obtainLabel(labelMap, label));
                continue;
            }

            // visit<...>Insn
            int opcode = Node.asValue(instruction.get(NodeConstants.OPCODE)).getValueAsInt();
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
                    int operand = Node.asValue(instruction.get(NodeConstants.OPERAND)).getValueAsInt();
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
                    String varName = Node.asValue(instruction.get(NodeConstants.VAR)).getValueAsString();
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
                    String type = Node.asValue(instruction.get(NodeConstants.TYPE)).getValueAsString();
                    methodVisitor.visitTypeInsn(opcode, type);
                    break;
                }
                case Opcodes.GETSTATIC:
                case Opcodes.PUTSTATIC:
                case Opcodes.GETFIELD:
                case Opcodes.PUTFIELD: {
                    // visitFieldInsn
                    String owner = Node.asValue(instruction.get(NodeConstants.OWNER)).getValueAsString();
                    String name1 = Node.asValue(instruction.get(NodeConstants.NAME)).getValueAsString();
                    String descriptor1 =
                            Node.asValue(instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    methodVisitor.visitFieldInsn(opcode, owner, name1, descriptor1);
                    break;
                }
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEINTERFACE: {
                    // visitMethodInsns
                    String owner = Node.asValue(instruction.get(NodeConstants.OWNER)).getValueAsString();
                    String name1 = Node.asValue(instruction.get(NodeConstants.NAME)).getValueAsString();
                    String descriptor1 =
                            Node.asValue(instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    boolean isInterface =
                            Node.asValue(instruction.get(NodeConstants.IS_INTERFACE)).getValueAsBoolean();
                    methodVisitor.visitMethodInsn(opcode, owner, name1, descriptor1, isInterface);
                    break;
                }
                case Opcodes.INVOKEDYNAMIC: {
                    // visitInvokeDynamicInsn
                    String name1 = Node.asValue(instruction.get(NodeConstants.NAME)).getValueAsString();
                    String descriptor1 =
                            Node.asValue(instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    Handle handle = getHandle(Node.asMap(instruction.get(NodeConstants.HANDLE)));
                    Object[] arguments = getArguments(Node.asList(instruction.get(NodeConstants.ARGUMENTS)));
                    methodVisitor.visitInvokeDynamicInsn(name1, descriptor1, handle, arguments);
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
                    String labelString = Node.asValue(instruction.get(NodeConstants.TARGET)).getValueAsString();
                    Label label = obtainLabel(labelMap, labelString);
                    methodVisitor.visitJumpInsn(opcode, label);
                    break;
                }
                case Opcodes.LDC: {
                    // visitLdcInsn
                    Object value = Node.asValue(instruction.get(NodeConstants.VALUE)).getValue();
                    methodVisitor.visitLdcInsn(value);
                    break;
                }
                case Opcodes.IINC: {
                    // visitIincInsn
                    String varName = Node.asValue(instruction.get(NodeConstants.VAR)).getValueAsString();
                    int varIndex = getLocalIndex(nextLocalIndex, remappedLocalIndexes, varName, 1);
                    int increment = Node.asValue(instruction.get(NodeConstants.INCREMENT)).getValueAsInt();
                    methodVisitor.visitIincInsn(varIndex, increment);
                    break;
                }
                case Opcodes.TABLESWITCH:
                case Opcodes.LOOKUPSWITCH: {
                    // visitTableSwitchInsn / visitLookupSwitchInsn
                    String defaultString =
                            Node.asValue(instruction.get(NodeConstants.DEFAULT)).getValueAsString();
                    Label dflt = obtainLabel(labelMap, defaultString);
                    ListNode cases = Node.asList(instruction.get(NodeConstants.CASES));
                    int[] keys = new int[cases.size()];
                    Label[] labels = new Label[cases.size()];
                    for (int i = 0; i < cases.size(); i++) {
                        MapNode caseNode = Node.asMap(cases.get(i));
                        keys[i] = Node.asValue(caseNode.get(NodeConstants.KEY)).getValueAsInt();
                        String caseLabelString = Node.asValue(caseNode.get(NodeConstants.LABEL)).getValueAsString();
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
                    String descriptor1 =
                            Node.asValue(instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    int dimensions = Node.asValue(instruction.get(NodeConstants.DIMENSIONS)).getValueAsInt();
                    methodVisitor.visitMultiANewArrayInsn(descriptor1, dimensions);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown instruction opcode");
            }

            // visitInsnAnnotation
            ListNode annotations = Node.asList(instruction.get(NodeConstants.ANNOTATIONS));
            if (annotations != null) {
                for (Node annotation : annotations) {
                    AnnotationNodeReader writer = new AnnotationNodeReader(annotation);
                    writer.visitAnnotation(null, methodVisitor::visitTypeAnnotation);
                }
            }
        }
    }

    private static int getLocalIndex(int[] nextLocalIndex, Map<String, Integer> remappedLocalIndexes, String varName, int size) {
        // TODO: smart merging of local variable indexes
        return remappedLocalIndexes.computeIfAbsent(varName, k -> {
            int index = nextLocalIndex[0];
            nextLocalIndex[0] += size;
            return index;
        });
    }

    private static void visitTryCatchBlocks(MethodVisitor methodVisitor, MapNode codeNode,
                                            Map<String, Label> labelMap) {
        ListNode tryCatchBlocksListNode = Node.asList(codeNode.get(NodeConstants.TRY_CATCH_BLOCKS));
        if (tryCatchBlocksListNode == null) {
            return;
        }
        for (Node n : tryCatchBlocksListNode) {
            MapNode tryCatchBlock = Node.asMap(n);

            String start = Node.asValue(tryCatchBlock.get(NodeConstants.START)).getValueAsString();
            String end = Node.asValue(tryCatchBlock.get(NodeConstants.END)).getValueAsString();
            String handler = Node.asValue(tryCatchBlock.get(NodeConstants.HANDLER)).getValueAsString();
            String type = Node.asValue(tryCatchBlock.get(NodeConstants.TYPE)).getValueAsString();

            Label startLabel = labelMap.computeIfAbsent(start, s -> new Label());
            Label endLabel = labelMap.computeIfAbsent(end, s -> new Label());
            Label handlerLabel = labelMap.computeIfAbsent(handler, s -> new Label());

            methodVisitor.visitTryCatchBlock(startLabel, endLabel, handlerLabel, type);

            // visitTryCatchBlockAnnotations
            for (Node n2 : Node.asList(tryCatchBlock.get(NodeConstants.ANNOTATIONS))) {
                AnnotationNodeReader writer = new AnnotationNodeReader(n2);
                writer.visitAnnotation(null, methodVisitor::visitTryCatchAnnotation);
            }
        }
    }

    private void visitLocalVariables(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        ListNode codeLocalsNode = Node.asList(codeNode.get(NodeConstants.SOURCE_LOCALS));
        if (codeLocalsNode == null) {
            return;
        }
        for (Node n : codeLocalsNode) {
            MapNode localNode = Node.asMap(n);
            String localName = Node.asValue(localNode.get(NodeConstants.NAME)).getValueAsString();
            String localDesc = Node.asValue(localNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();

            ValueNode localSignatureNode = Node.asValue(localNode.get(NodeConstants.SIGNATURE));
            String localSignature = localSignatureNode == null ? localDesc : localSignatureNode.getValueAsString();

            String start = Node.asValue(localNode.get(NodeConstants.START)).getValueAsString();
            String end = Node.asValue(localNode.get(NodeConstants.END)).getValueAsString();
            int index = Node.asValue(localNode.get(NodeConstants.INDEX)).getValueAsInt();

            Label startLabel = labelMap.get(start);
            Label endLabel = labelMap.get(end);

            methodVisitor
                    .visitLocalVariable(localName, localDesc, localSignature, startLabel, endLabel, index);

            // visitLocalVariableAnnotation
            // TODO
        }
    }

    private void visitLineNumbers(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        ListNode lineNumbers = Node.asList(codeNode.get(NodeConstants.LINE_NUMBERS));
        if (lineNumbers != null) {
            for (Node n : lineNumbers) {
                MapNode lineNumber = Node.asMap(n);
                int line = Node.asValue(lineNumber.get(NodeConstants.LINE)).getValueAsInt();
                String labelName = Node.asValue(lineNumber.get(NodeConstants.LABEL)).getValueAsString();
                Label label = labelMap.get(labelName);
                if (label != null) {
                    methodVisitor.visitLineNumber(line, label);
                }
            }
        }
    }

    private void visitAttributes(MethodVisitor methodVisitor) {
        ListNode methodAttributesNode = Node.asList(methodNode.get(NodeConstants.ATTRIBUTES));
        if (methodAttributesNode != null) {
            for (Node n : methodAttributesNode) {
                methodVisitor.visitAttribute(Node.asValue(n).getValueAs(Attribute.class));
            }
        }
    }

    private void visitAnnotations(MethodVisitor methodVisitor) {
        ListNode methodAnnotationsNode = Node.asList(methodNode.get(NodeConstants.ANNOTATIONS));
        if (methodAnnotationsNode != null) {
            for (Node n : methodAnnotationsNode) {
                AnnotationNodeReader writer = new AnnotationNodeReader(n);
                writer.visitAnnotation(methodVisitor::visitAnnotation, methodVisitor::visitTypeAnnotation);
            }
        }
    }

    private void visitAnnotationDefault(MethodVisitor methodVisitor) {
        if (methodNode.containsKey(NodeConstants.ANNOTATION_DEFAULT)) {
            AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
            AnnotationNodeReader writer =
                    new AnnotationNodeReader(methodNode.get(NodeConstants.ANNOTATION_DEFAULT));
            writer.visitAnnotation(annotationVisitor);
        }
    }

    private void visitParameterAnnotations(MethodVisitor methodVisitor) {
        ListNode parameters = Node.asList(methodNode.get(NodeConstants.PARAMETERS));

        // visitAnnotableParameterCount
        // We make all parameters annotable
        methodVisitor.visitAnnotableParameterCount(parameters.size(), true);
        methodVisitor.visitAnnotableParameterCount(parameters.size(), false);

        // visitParameterAnnotation
        for (int i = 0; i < parameters.size(); i++) {
            MapNode parameterNode = Node.asMap(parameters.get(i));
            ListNode annotations = Node.asList(parameterNode.get(NodeConstants.ANNOTATIONS));
            if (annotations != null) {
                for (Node node : annotations) {
                    MapNode annotationNode = Node.asMap(node);
                    String descriptor = Node.asValue(annotationNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    ValueNode visible = Node.asValue(annotationNode.get(NodeConstants.VISIBLE));

                    AnnotationNodeReader reader = new AnnotationNodeReader(node);
                    AnnotationVisitor visitor = methodVisitor.visitParameterAnnotation(i, descriptor,
                            visible == null || visible.getValueAsBoolean());
                    reader.visitAnnotation(visitor);
                }
            }
        }
    }

    private void visitParameters(MethodVisitor methodVisitor) {
        ListNode methodParametersNode = Node.asList(methodNode.get(NodeConstants.PARAMETERS));
        for (Node n : methodParametersNode) {
            MapNode parameterNode = Node.asMap(n);
            ValueNode nameNode = Node.asValue(parameterNode.get(NodeConstants.NAME));
            ValueNode indexNode = Node.asValue(parameterNode.get(NodeConstants.ACCESS));
            methodVisitor.visitParameter(
                    nameNode == null ? null : nameNode.getValueAsString(),
                    indexNode == null ? 0 : indexNode.getValueAsInt()
            );
        }
    }

    public void visitMethod(ClassVisitor visitor) {
        int access = Node.asValue(methodNode.get(NodeConstants.ACCESS)).getValueAsInt();
        String name = Node.asValue(methodNode.get(NodeConstants.NAME)).getValueAsString();

        Type returnType = Node.asValue(methodNode.get(NodeConstants.RETURN_TYPE)).getValueAs(Type.class);
        ListNode parameters = Node.asList(methodNode.get(NodeConstants.PARAMETERS));
        Type[] parameterTypes = new Type[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            MapNode parameterNode = Node.asMap(parameters.get(i));
            parameterTypes[i] = Node.asValue(parameterNode.get(NodeConstants.TYPE)).getValueAs(Type.class);
        }
        String descriptor = Type.getMethodDescriptor(returnType, parameterTypes);

        ValueNode signatureNode = Node.asValue(methodNode.get(NodeConstants.SIGNATURE));
        String signature = signatureNode == null ? null : signatureNode.getValueAsString();

        ListNode exceptionsNode = Node.asList(methodNode.get(NodeConstants.EXCEPTIONS));
        String[] exceptions = exceptionsNode == null ? new String[0]
                : exceptionsNode.stream().map(n -> Node.asValue(n).getValueAsString()).toArray(String[]::new);
        MethodVisitor methodVisitor = visitor.visitMethod(access, name, descriptor, signature, exceptions);

        // visitParameter
        visitParameters(methodVisitor);

        // visitAnnotableParameterCount/visitParameterAnnotation
        visitParameterAnnotations(methodVisitor);

        // visitAnnotationDefault
        visitAnnotationDefault(methodVisitor);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(methodVisitor);

        // visitAttribute
        visitAttributes(methodVisitor);

        // visitCode
        if (methodNode.containsKey(NodeConstants.CODE)) {
            MapNode codeNode = Node.asMap(methodNode.get(NodeConstants.CODE));
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
