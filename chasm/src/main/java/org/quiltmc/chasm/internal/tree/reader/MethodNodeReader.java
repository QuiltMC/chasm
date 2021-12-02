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

    public static Object[] getArguments(ListNode argumentNode) {
        Object[] arguments = new Object[argumentNode.size()];
        for (int i = 0; i < arguments.length; i++) {
            Node argNode = argumentNode.get(i);
            if (argNode instanceof ValueNode) {
                arguments[i] = ((ValueNode) argNode).getValue();
            } else if (((MapNode) argNode).containsKey(NodeConstants.TAG)) {
                arguments[i] = getHandle((MapNode) argNode);
            } else {
                MapNode constDynamicNode = (MapNode) argNode;
                String name = ((ValueNode) constDynamicNode.get(NodeConstants.NAME)).getValueAsString();
                String descriptor = ((ValueNode) constDynamicNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                Handle handle = getHandle((MapNode) constDynamicNode.get(NodeConstants.HANDLE));
                Object[] args = getArguments((ListNode) constDynamicNode.get(NodeConstants.ARGS));
                arguments[i] = new ConstantDynamic(name, descriptor, handle, args);
            }
        }

        return arguments;
    }

    public static Handle getHandle(MapNode handleNode) {
        int tag = ((ValueNode) handleNode.get(NodeConstants.TAG)).getValueAsInt();
        String owner = ((ValueNode) handleNode.get(NodeConstants.OWNER)).getValueAsString();
        String name = ((ValueNode) handleNode.get(NodeConstants.NAME)).getValueAsString();
        String descriptor = ((ValueNode) handleNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();
        boolean isInterface = ((ValueNode) handleNode.get(NodeConstants.IS_INTERFACE)).getValueAsBoolean();

        return new Handle(tag, owner, name, descriptor, isInterface);
    }

    private static Label obtainLabel(Map<String, Label> labelMap, String labelName) {
        return labelMap.computeIfAbsent(labelName, unusedLabelName -> new Label());
    }

    private static void visitInstructions(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        for (Node rawInstruction : (ListNode) codeNode.get(NodeConstants.INSTRUCTIONS)) {
            MapNode instruction = (MapNode) rawInstruction;

            // visitLabel
            if (instruction.containsKey(NodeConstants.LABEL)) {
                String label = ((ValueNode) instruction.get(NodeConstants.LABEL)).getValueAsString();
                methodVisitor.visitLabel(obtainLabel(labelMap, label));
                continue;
            }

            // visit<...>Insn
            int opcode = ((ValueNode) instruction.get(NodeConstants.OPCODE)).getValueAsInt();
            switch (opcode) {
                case Opcodes.NOP:
                    // TODO Hack to strip the trailing NOP
                    break;
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
                    int operand = ((ValueNode) instruction.get(NodeConstants.OPERAND)).getValueAsInt();
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
                    int varIndex = ((ValueNode) instruction.get(NodeConstants.VAR)).getValueAsInt();
                    methodVisitor.visitVarInsn(opcode, varIndex);
                    break;
                }
                case Opcodes.NEW:
                case Opcodes.ANEWARRAY:
                case Opcodes.CHECKCAST:
                case Opcodes.INSTANCEOF: {
                    // visitTypeInsn
                    String type = ((ValueNode) instruction.get(NodeConstants.TYPE)).getValueAsString();
                    methodVisitor.visitTypeInsn(opcode, type);
                    break;
                }
                case Opcodes.GETSTATIC:
                case Opcodes.PUTSTATIC:
                case Opcodes.GETFIELD:
                case Opcodes.PUTFIELD: {
                    // visitFieldInsn
                    String owner = ((ValueNode) instruction.get(NodeConstants.OWNER)).getValueAsString();
                    String name1 = ((ValueNode) instruction.get(NodeConstants.NAME)).getValueAsString();
                    String descriptor1 =
                            ((ValueNode) instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    methodVisitor.visitFieldInsn(opcode, owner, name1, descriptor1);
                    break;
                }
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEINTERFACE: {
                    // visitMethodInsns
                    String owner = ((ValueNode) instruction.get(NodeConstants.OWNER)).getValueAsString();
                    String name1 = ((ValueNode) instruction.get(NodeConstants.NAME)).getValueAsString();
                    String descriptor1 =
                            ((ValueNode) instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    boolean isInterface =
                            ((ValueNode) instruction.get(NodeConstants.IS_INTERFACE)).getValueAsBoolean();
                    methodVisitor.visitMethodInsn(opcode, owner, name1, descriptor1, isInterface);
                    break;
                }
                case Opcodes.INVOKEDYNAMIC: {
                    // visitInvokeDynamicInsn
                    String name1 = ((ValueNode) instruction.get(NodeConstants.NAME)).getValueAsString();
                    String descriptor1 =
                            ((ValueNode) instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    Handle handle = getHandle((MapNode) instruction.get(NodeConstants.HANDLE));
                    Object[] arguments = getArguments((ListNode) instruction.get(NodeConstants.ARGUMENTS));
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
                    String labelString = ((ValueNode) instruction.get(NodeConstants.TARGET)).getValueAsString();
                    Label label = obtainLabel(labelMap, labelString);
                    methodVisitor.visitJumpInsn(opcode, label);
                    break;
                }
                case Opcodes.LDC: {
                    // visitLdcInsn
                    Object value = ((ValueNode) instruction.get(NodeConstants.VALUE)).getValue();
                    methodVisitor.visitLdcInsn(value);
                    break;
                }
                case Opcodes.IINC: {
                    // visitIincInsn
                    int varIndex = ((ValueNode) instruction.get(NodeConstants.VAR)).getValueAsInt();
                    int increment = ((ValueNode) instruction.get(NodeConstants.INCREMENT)).getValueAsInt();
                    methodVisitor.visitIincInsn(varIndex, increment);
                    break;
                }
                case Opcodes.TABLESWITCH:
                case Opcodes.LOOKUPSWITCH: {
                    // visitTableSwitchInsn / visitLookupSwitchInsn
                    String defaultString =
                            ((ValueNode) instruction.get(NodeConstants.DEFAULT)).getValueAsString();
                    Label dflt = obtainLabel(labelMap, defaultString);
                    ListNode cases = (ListNode) instruction.get(NodeConstants.CASES);
                    int[] keys = new int[cases.size()];
                    Label[] labels = new Label[cases.size()];
                    for (int i = 0; i < cases.size(); i++) {
                        MapNode caseNode = (MapNode) cases.get(i);
                        keys[i] = ((ValueNode) caseNode.get(NodeConstants.KEY)).getValueAsInt();
                        String caseLabelString = ((ValueNode) caseNode.get(NodeConstants.LABEL)).getValueAsString();
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
                            ((ValueNode) instruction.get(NodeConstants.DESCRIPTOR)).getValueAsString();
                    int dimensions = ((ValueNode) instruction.get(NodeConstants.DIMENSIONS)).getValueAsInt();
                    methodVisitor.visitMultiANewArrayInsn(descriptor1, dimensions);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown instruction opcode");
            }

            // visitInsnAnnotation
            ListNode annotations = (ListNode) instruction.get(NodeConstants.ANNOTATIONS);
            if (annotations != null) {
                for (Node annotation : annotations) {
                    AnnotationNodeReader writer = new AnnotationNodeReader(annotation);
                    writer.visitAnnotation(null, methodVisitor::visitTypeAnnotation);
                }
            }
        }
    }

    private static void visitTryCatchBlocks(MethodVisitor methodVisitor, MapNode codeNode,
                                            Map<String, Label> labelMap) {
        ListNode tryCatchBlocksListNode = (ListNode) codeNode.get(NodeConstants.TRY_CATCH_BLOCKS);
        if (tryCatchBlocksListNode == null) {
            return;
        }
        for (Node n : tryCatchBlocksListNode) {
            MapNode tryCatchBlock = (MapNode) n;

            String start = ((ValueNode) tryCatchBlock.get(NodeConstants.START)).getValueAsString();
            String end = ((ValueNode) tryCatchBlock.get(NodeConstants.END)).getValueAsString();
            String handler = ((ValueNode) tryCatchBlock.get(NodeConstants.HANDLER)).getValueAsString();
            String type = ((ValueNode) tryCatchBlock.get(NodeConstants.TYPE)).getValueAsString();

            Label startLabel = labelMap.computeIfAbsent(start, s -> new Label());
            Label endLabel = labelMap.computeIfAbsent(end, s -> new Label());
            Label handlerLabel = labelMap.computeIfAbsent(handler, s -> new Label());

            methodVisitor.visitTryCatchBlock(startLabel, endLabel, handlerLabel, type);

            // visitTryCatchBlockAnnotations
            for (Node n2 : (ListNode) tryCatchBlock.get(NodeConstants.ANNOTATIONS)) {
                AnnotationNodeReader writer = new AnnotationNodeReader(n2);
                writer.visitAnnotation(null, methodVisitor::visitTryCatchAnnotation);
            }
        }
    }

    private void visitLocalVariables(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        ListNode codeLocalsNode = (ListNode) codeNode.get(NodeConstants.LOCALS);
        if (codeLocalsNode == null) {
            return;
        }
        for (Node n : codeLocalsNode) {
            MapNode localNode = (MapNode) n;
            String localName = ((ValueNode) localNode.get(NodeConstants.NAME)).getValueAsString();
            String localDesc = ((ValueNode) localNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();

            ValueNode localSignatureNode = (ValueNode) localNode.get(NodeConstants.SIGNATURE);
            String localSignature = localSignatureNode == null ? localDesc : localSignatureNode.getValueAsString();

            String start = ((ValueNode) localNode.get(NodeConstants.START)).getValueAsString();
            String end = ((ValueNode) localNode.get(NodeConstants.END)).getValueAsString();
            int index = ((ValueNode) localNode.get(NodeConstants.INDEX)).getValueAsInt();

            Label startLabel = labelMap.get(start);
            Label endLabel = labelMap.get(end);

            methodVisitor
                    .visitLocalVariable(localName, localDesc, localSignature, startLabel, endLabel, index);

            // visitLocalVariableAnnotation
            // TODO
        }
    }

    private void visitLineNumbers(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        ListNode lineNumbers = (ListNode) codeNode.get(NodeConstants.LINE_NUMBERS);
        if (lineNumbers != null) {
            for (Node n : lineNumbers) {
                MapNode lineNumber = (MapNode) n;
                int line = ((ValueNode) lineNumber.get(NodeConstants.LINE)).getValueAsInt();
                String labelName = ((ValueNode) lineNumber.get(NodeConstants.LABEL)).getValueAsString();
                Label label = labelMap.get(labelName);
                if (label != null) {
                    methodVisitor.visitLineNumber(line, label);
                }
            }
        }
    }

    private void visitAttributes(MethodVisitor methodVisitor) {
        ListNode methodAttributesNode = (ListNode) methodNode.get(NodeConstants.ATTRIBUTES);
        if (methodAttributesNode != null) {
            for (Node n : methodAttributesNode) {
                methodVisitor.visitAttribute(((ValueNode) n).getValueAs(Attribute.class));
            }
        }
    }

    private void visitAnnotations(MethodVisitor methodVisitor) {
        ListNode methodAnnotationsNode = (ListNode) methodNode.get(NodeConstants.ANNOTATIONS);
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
        // visitParameterAnnotation
        int visibleCount = 0;
        int invisibleCount = 0;
        ListNode parameterAnnotationsListNode = (ListNode) methodNode.get(NodeConstants.PARAMETER_ANNOTATIONS);
        if (parameterAnnotationsListNode == null) {
            methodVisitor.visitAnnotableParameterCount(0, true);
            methodVisitor.visitAnnotableParameterCount(0, false);
            return;
        }
        for (Node n : parameterAnnotationsListNode) {
            MapNode annotationNode = (MapNode) n;
            int parameter = ((ValueNode) annotationNode.get(NodeConstants.PARAMETER)).getValueAsInt();
            String annotationDesc = ((ValueNode) annotationNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();

            ValueNode methodAnnotationVisibilityNode = (ValueNode) annotationNode
                    .get(NodeConstants.VISIBLE);
            boolean visible = methodAnnotationVisibilityNode == null
                    || methodAnnotationVisibilityNode.getValueAsBoolean();

            AnnotationNodeReader writer = new AnnotationNodeReader(n);
            AnnotationVisitor annotationVisitor =
                    methodVisitor.visitParameterAnnotation(parameter, annotationDesc, visible);
            writer.visitAnnotation(annotationVisitor);

            if (visible) {
                visibleCount++;
            } else {
                invisibleCount++;
            }
        }

        // visitAnnotableParameterCount
        methodVisitor.visitAnnotableParameterCount(visibleCount, true);
        methodVisitor.visitAnnotableParameterCount(invisibleCount, false);
    }

    private void visitParameters(MethodVisitor methodVisitor) {
        ListNode methodParametersNode = (ListNode) methodNode.get(NodeConstants.PARAMETERS);
        if (methodParametersNode == null) {
            return;
        }
        for (Node n : methodParametersNode) {
            MapNode parameterNode = (MapNode) n;
            String parameterName = ((ValueNode) parameterNode.get(NodeConstants.NAME)).getValueAsString();
            int parameterAccess = ((ValueNode) parameterNode.get(NodeConstants.ACCESS)).getValueAsInt();
            methodVisitor.visitParameter(parameterName, parameterAccess);
        }
    }

    public void visitMethod(ClassVisitor visitor) {
        int access = ((ValueNode) methodNode.get(NodeConstants.ACCESS)).getValueAsInt();
        String name = ((ValueNode) methodNode.get(NodeConstants.NAME)).getValueAsString();
        String descriptor = ((ValueNode) methodNode.get(NodeConstants.DESCRIPTOR)).getValueAsString();

        ValueNode signatureNode = (ValueNode) methodNode.get(NodeConstants.SIGNATURE);
        String signature = signatureNode == null ? null : signatureNode.getValueAsString();

        ListNode exceptionsNode = (ListNode) methodNode.get(NodeConstants.EXCEPTIONS);
        String[] exceptions = exceptionsNode == null ? new String[0]
                : exceptionsNode.stream().map(n -> ((ValueNode) n).getValueAsString()).toArray(String[]::new);
        MethodVisitor methodVisitor = visitor.visitMethod(access, name, descriptor, signature, exceptions);

        // visitParameter
        visitParameters(methodVisitor);

        visitParameterAnnotations(methodVisitor);

        // visitAnnotationDefault
        visitAnnotationDefault(methodVisitor);

        // visitAnnotation/visitTypeAnnotation
        visitAnnotations(methodVisitor);

        // visitAttribute
        visitAttributes(methodVisitor);

        // visitCode
        if (methodNode.containsKey(NodeConstants.CODE)) {
            MapNode codeNode = (MapNode) methodNode.get(NodeConstants.CODE);
            Map<String, Label> labelMap = new HashMap<>();

            // visitFrame
            // Don't care

            // Instructions
            visitInstructions(methodVisitor, codeNode, labelMap);

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
