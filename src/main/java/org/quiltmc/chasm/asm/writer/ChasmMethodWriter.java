package org.quiltmc.chasm.asm.writer;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

@SuppressWarnings("unchecked")
public class ChasmMethodWriter {
    private final MapNode methodNode;

    public ChasmMethodWriter(MapNode methodNode) {
        this.methodNode = methodNode;
    }

    private void visitInstructions(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        for (Node n : (ListNode) codeNode.get(NodeConstants.INSTRUCTIONS)) {
            // visitLabel
            ListNode labelsNode = (ListNode) ((MapNode) n).get(NodeConstants.LABELS);
            for (Node n2 : labelsNode) {
                methodVisitor.visitLabel(
                        labelMap.computeIfAbsent(n2.getAsString(), s -> new Label()));
            }

            if (((MapNode) n).containsKey(NodeConstants.LINE)) {
                if (labelsNode.isEmpty()) {
                    throw new RuntimeException("Encountered line number without label.");
                }
                int line = ((MapNode) n).get(NodeConstants.LINE).getAsInt();
                methodVisitor
                        .visitLineNumber(line, labelMap.get(labelsNode.get(0).getAsString()));
            }

            // visit<...>Insn
            int opcode = ((MapNode) n).get(NodeConstants.OPCODE).getAsInt();
            switch (opcode) {
                case Opcodes.NOP:
                    // TODO: This is a hack to strip trailing nops added earlier
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
                    int operand = ((MapNode) n).get(NodeConstants.OPERAND).getAsInt();
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
                    int var = ((MapNode) n).get(NodeConstants.VAR).getAsInt();
                    methodVisitor.visitVarInsn(opcode, var);
                    break;
                }
                case Opcodes.NEW:
                case Opcodes.ANEWARRAY:
                case Opcodes.CHECKCAST:
                case Opcodes.INSTANCEOF: {
                    // visitTypeInsn
                    String type = ((MapNode) n).get(NodeConstants.TYPE).getAsString();
                    methodVisitor.visitTypeInsn(opcode, type);
                    break;
                }
                case Opcodes.GETSTATIC:
                case Opcodes.PUTSTATIC:
                case Opcodes.GETFIELD:
                case Opcodes.PUTFIELD: {
                    // visitFieldInsn
                    String owner = ((MapNode) n).get(NodeConstants.OWNER).getAsString();
                    String name1 = ((MapNode) n).get(NodeConstants.NAME).getAsString();
                    String descriptor1 =
                            ((MapNode) n).get(NodeConstants.DESCRIPTOR).getAsString();
                    methodVisitor.visitFieldInsn(opcode, owner, name1, descriptor1);
                    break;
                }
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEINTERFACE: {
                    // visitMethodInsns
                    String owner = ((MapNode) n).get(NodeConstants.OWNER).getAsString();
                    String name1 = ((MapNode) n).get(NodeConstants.NAME).getAsString();
                    String descriptor1 =
                            ((MapNode) n).get(NodeConstants.DESCRIPTOR).getAsString();
                    Boolean isInterface =
                            ((MapNode) n).get(NodeConstants.IS_INTERFACE).getAsBoolean();
                    methodVisitor.visitMethodInsn(opcode, owner, name1, descriptor1, isInterface);
                    break;
                }
                case Opcodes.INVOKEDYNAMIC: {
                    // visitInvokeDynamicInsn
                    String name1 = ((MapNode) n).get(NodeConstants.NAME).getAsString();
                    String descriptor1 =
                            ((MapNode) n).get(NodeConstants.DESCRIPTOR).getAsString();
                    Handle handle = ChasmClassWriter.getHandle((MapNode) ((MapNode) n).get(NodeConstants.HANDLE));
                    Object[] arguments =
                            ChasmClassWriter.getArguments((ListNode) ((MapNode) n).get(NodeConstants.ARGUMENTS));
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
                    String labelString = ((MapNode) n).get(NodeConstants.TARGET).getAsString();
                    Label label = labelMap.computeIfAbsent(labelString, s -> new Label());
                    methodVisitor.visitJumpInsn(opcode, label);
                    break;
                }
                case Opcodes.LDC: {
                    // visitLdcInsn
                    Object value = ((MapNode) n).get(NodeConstants.VALUE).getAsObject();
                    methodVisitor.visitLdcInsn(value);
                    break;
                }
                case Opcodes.IINC: {
                    // visitIincInsn
                    int var = ((MapNode) n).get(NodeConstants.VAR).getAsInt();
                    int increment = ((MapNode) n).get(NodeConstants.INCREMENT).getAsInt();
                    methodVisitor.visitIincInsn(var, increment);
                    break;
                }
                case Opcodes.TABLESWITCH:
                case Opcodes.LOOKUPSWITCH: {
                    // visitTableSwitchInsn / visitLookupSwitchInsn
                    String defaultString =
                            ((MapNode) n).get(NodeConstants.DEFAULT).getAsString();
                    Label dflt = labelMap.computeIfAbsent(defaultString, s -> new Label());
                    ListNode cases = (ListNode) ((MapNode) n).get(NodeConstants.CASES);
                    int[] keys = new int[cases.size()];
                    Label[] labels = new Label[cases.size()];
                    for (int i = 0; i < cases.size(); i++) {
                        MapNode caseNode = (MapNode) cases.get(i);
                        keys[i] = caseNode.get(NodeConstants.KEY).getAsInt();
                        String caseLabelString = caseNode.get(NodeConstants.LABEL).getAsString();
                        labels[i] = labelMap.computeIfAbsent(caseLabelString, s -> new Label());
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
                            ((MapNode) n).get(NodeConstants.DESCRIPTOR).getAsString();
                    int dimensions = ((MapNode) n).get(NodeConstants.DIMENSIONS).getAsInt();
                    methodVisitor.visitMultiANewArrayInsn(descriptor1, dimensions);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown instruction opcode");
            }

            // visitInsnAnnotation
            for (Node n1 : (ListNode) ((MapNode) n).get(NodeConstants.ANNOTATIONS)) {
                ChasmAnnotationWriter writer = new ChasmAnnotationWriter((MapNode) n1);
                writer.visitAnnotation(null, methodVisitor::visitTypeAnnotation);
            }
        }
    }

    private void visitLocalVariables(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        for (Node n : (ListNode) codeNode.get(NodeConstants.LOCALS)) {
            MapNode localNode = (MapNode) n;
            String localName = localNode.get(NodeConstants.NAME).getAsString();
            String localDesc = localNode.get(NodeConstants.DESCRIPTOR).getAsString();
            String localSignature = localNode.get(NodeConstants.SIGNATURE).getAsString();
            String start = localNode.get(NodeConstants.START).getAsString();
            String end = localNode.get(NodeConstants.END).getAsString();
            int index = localNode.get(NodeConstants.INDEX).getAsInt();

            Label startLabel = labelMap.get(start);
            Label endLabel = labelMap.get(end);

            methodVisitor
                    .visitLocalVariable(localName, localDesc, localSignature, startLabel, endLabel, index);

            // visitLocalVariableAnnotation
            // TODO
        }
    }

    private void visitTryCatchBlocks(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        for (Node n : (ListNode) codeNode.get(NodeConstants.TRY_CATCH_BLOCKS)) {
            MapNode tryCatchBlock = (MapNode) n;

            String start = tryCatchBlock.get(NodeConstants.START).getAsString();
            String end = tryCatchBlock.get(NodeConstants.END).getAsString();
            String handler = tryCatchBlock.get(NodeConstants.HANDLER).getAsString();
            String type = tryCatchBlock.get(NodeConstants.TYPE).getAsString();

            Label startLabel = labelMap.computeIfAbsent(start, s -> new Label());
            Label endLabel = labelMap.computeIfAbsent(end, s -> new Label());
            Label handlerLabel = labelMap.computeIfAbsent(handler, s -> new Label());

            methodVisitor.visitTryCatchBlock(startLabel, endLabel, handlerLabel, type);

            // visitTryCatchBlockAnnotations
            for (Node n2 : (ListNode) tryCatchBlock.get(NodeConstants.ANNOTATIONS)) {
                ChasmAnnotationWriter writer = new ChasmAnnotationWriter((MapNode) n2);
                writer.visitAnnotation(null, methodVisitor::visitTryCatchAnnotation);
            }
        }
    }

    private void visitAttributes(MethodVisitor methodVisitor) {
        for (Node n : (ListNode) methodNode.get(NodeConstants.ATTRIBUTES)) {
            methodVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }
    }

    private void visitAnnotations(MethodVisitor methodVisitor) {
        for (Node n : (ListNode) methodNode.get(NodeConstants.ANNOTATIONS)) {
            ChasmAnnotationWriter writer = new ChasmAnnotationWriter((MapNode) n);
            writer.visitAnnotation(methodVisitor::visitAnnotation, methodVisitor::visitTypeAnnotation);
        }
    }

    private void visitAnnotationDefault(MethodVisitor methodVisitor) {
        if (methodNode.containsKey(NodeConstants.ANNOTATION_DEFAULT)) {
            AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
            ChasmAnnotationWriter writer =
                    new ChasmAnnotationWriter((MapNode) methodNode.get(NodeConstants.ANNOTATION_DEFAULT));
            writer.visitAnnotation(annotationVisitor);
        }
    }

    private void visitParameterAnnotations(MethodVisitor methodVisitor) {
        // visitParameterAnnotation
        int visibleCount = 0;
        int invisibleCount = 0;
        for (Node n : (ListNode) methodNode.get(NodeConstants.PARAMETER_ANNOTATIONS)) {
            MapNode annotationNode = (MapNode) n;
            int parameter = annotationNode.get(NodeConstants.PARAMETER).getAsInt();
            String annotationDesc = annotationNode.get(NodeConstants.DESCRIPTOR).getAsString();
            boolean visible = annotationNode.get(NodeConstants.VISIBLE).getAsBoolean();

            ChasmAnnotationWriter writer = new ChasmAnnotationWriter((MapNode) n);
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
        for (Node n : (ListNode) methodNode.get(NodeConstants.PARAMETERS)) {
            MapNode parameterNode = (MapNode) n;
            String parameterName = parameterNode.get(NodeConstants.NAME).getAsString();
            int parameterAccess = parameterNode.get(NodeConstants.ACCESS).getAsInt();
            methodVisitor.visitParameter(parameterName, parameterAccess);
        }
    }

    public void visitMethod(ClassVisitor visitor) {
        int access = methodNode.get(NodeConstants.ACCESS).getAsInt();
        String name = methodNode.get(NodeConstants.NAME).getAsString();
        String descriptor = methodNode.get(NodeConstants.DESCRIPTOR).getAsString();
        String signature = methodNode.get(NodeConstants.SIGNATURE).getAsString();
        String[] exceptions = ((ListNode) methodNode.get(NodeConstants.EXCEPTIONS))
                .stream().map(n -> n.getAsString()).toArray(String[]::new);

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
            // Don't care

            // visitMaxs
            methodVisitor.visitMaxs(0, 0);
        }

        // visitEnd
        methodVisitor.visitEnd();
    }
}
