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

    private static void visitInstructions(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        for (Node n : (ListNode) codeNode.get(NodeConstants.INSTRUCTIONS)) {
            // visitLabel
            ListNode labelsNode = (ListNode) ((MapNode) n).get(NodeConstants.LABELS);
            for (Node n2 : labelsNode) {
                methodVisitor.visitLabel(
                        labelMap.computeIfAbsent(((ValueNode<String>) n2).getValue(), s -> new Label()));
            }

            if (((MapNode) n).containsKey(NodeConstants.LINE)) {
                if (labelsNode.isEmpty()) {
                    throw new RuntimeException("Encountered line number without label.");
                }
                int line = ((ValueNode<Integer>) ((MapNode) n).get(NodeConstants.LINE)).getValue().intValue();
                methodVisitor
                        .visitLineNumber(line, labelMap.get(((ValueNode<String>) labelsNode.get(0)).getValue()));
            }

            // visit<...>Insn
            int opcode = ((ValueNode<Integer>) ((MapNode) n).get(NodeConstants.OPCODE)).getValue();
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
                    int operand = ((ValueNode<Integer>) ((MapNode) n).get(NodeConstants.OPERAND)).getValue().intValue();
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
                    int varValue = ((ValueNode<Integer>) ((MapNode) n).get(NodeConstants.VAR)).getValue();
                    methodVisitor.visitVarInsn(opcode, varValue);
                    break;
                }
                case Opcodes.NEW:
                case Opcodes.ANEWARRAY:
                case Opcodes.CHECKCAST:
                case Opcodes.INSTANCEOF: {
                    // visitTypeInsn
                    String type = ((ValueNode<String>) ((MapNode) n).get(NodeConstants.TYPE)).getValue();
                    methodVisitor.visitTypeInsn(opcode, type);
                    break;
                }
                case Opcodes.GETSTATIC:
                case Opcodes.PUTSTATIC:
                case Opcodes.GETFIELD:
                case Opcodes.PUTFIELD: {
                    // visitFieldInsn
                    String owner = ((ValueNode<String>) ((MapNode) n).get(NodeConstants.OWNER)).getValue();
                    String name1 = ((ValueNode<String>) ((MapNode) n).get(NodeConstants.NAME)).getValue();
                    String descriptor1 =
                            ((ValueNode<String>) ((MapNode) n).get(NodeConstants.DESCRIPTOR)).getValue();
                    methodVisitor.visitFieldInsn(opcode, owner, name1, descriptor1);
                    break;
                }
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKESTATIC:
                case Opcodes.INVOKEINTERFACE: {
                    // visitMethodInsns
                    String owner = ((ValueNode<String>) ((MapNode) n).get(NodeConstants.OWNER)).getValue();
                    String name1 = ((ValueNode<String>) ((MapNode) n).get(NodeConstants.NAME)).getValue();
                    String descriptor1 =
                            ((ValueNode<String>) ((MapNode) n).get(NodeConstants.DESCRIPTOR)).getValue();
                    Boolean isInterface =
                            ((ValueNode<Boolean>) ((MapNode) n).get(NodeConstants.IS_INTERFACE)).getValue();
                    methodVisitor.visitMethodInsn(opcode, owner, name1, descriptor1, isInterface);
                    break;
                }
                case Opcodes.INVOKEDYNAMIC: {
                    // visitInvokeDynamicInsn
                    String name1 = ((ValueNode<String>) ((MapNode) n).get(NodeConstants.NAME)).getValue();
                    String descriptor1 =
                            ((ValueNode<String>) ((MapNode) n).get(NodeConstants.DESCRIPTOR)).getValue();
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
                    String labelString = ((ValueNode<String>) ((MapNode) n).get(NodeConstants.TARGET)).getValue();
                    Label label = labelMap.computeIfAbsent(labelString, s -> new Label());
                    methodVisitor.visitJumpInsn(opcode, label);
                    break;
                }
                case Opcodes.LDC: {
                    // visitLdcInsn
                    Object value = ((ValueNode<Object>) ((MapNode) n).get(NodeConstants.VALUE)).getValue();
                    methodVisitor.visitLdcInsn(value);
                    break;
                }
                case Opcodes.IINC: {
                    // visitIincInsn
                    int var = ((ValueNode<Integer>) ((MapNode) n).get(NodeConstants.VAR)).getValue();
                    int increment = ((ValueNode<Integer>) ((MapNode) n).get(NodeConstants.INCREMENT)).getValue();
                    methodVisitor.visitIincInsn(var, increment);
                    break;
                }
                case Opcodes.TABLESWITCH:
                case Opcodes.LOOKUPSWITCH: {
                    // visitTableSwitchInsn / visitLookupSwitchInsn
                    String defaultString =
                            ((ValueNode<String>) ((MapNode) n).get(NodeConstants.DEFAULT)).getValue();
                    Label dflt = labelMap.computeIfAbsent(defaultString, s -> new Label());
                    ListNode cases = (ListNode) ((MapNode) n).get(NodeConstants.CASES);
                    int[] keys = new int[cases.size()];
                    Label[] labels = new Label[cases.size()];
                    for (int i = 0; i < cases.size(); i++) {
                        MapNode caseNode = (MapNode) cases.get(i);
                        keys[i] = ((ValueNode<Integer>) caseNode.get(NodeConstants.KEY)).getValue();
                        String caseLabelString = ((ValueNode<String>) caseNode.get(NodeConstants.LABEL)).getValue();
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
                            ((ValueNode<String>) ((MapNode) n).get(NodeConstants.DESCRIPTOR)).getValue();
                    int dimensions = ((ValueNode<Integer>) ((MapNode) n).get(NodeConstants.DIMENSIONS)).getValue().intValue();
                    methodVisitor.visitMultiANewArrayInsn(descriptor1, dimensions);
                    break;
                }
                default:
                    throw new RuntimeException("Unknown instruction opcode");
            }

            // visitInsnAnnotation
            for (Node n1 : (ListNode) ((MapNode) n).get(NodeConstants.ANNOTATIONS)) {
                ChasmAnnotationWriter writer = new ChasmAnnotationWriter(n1);
                writer.visitAnnotation(null, methodVisitor::visitTypeAnnotation);
            }
        }
    }

    private void visitLocalVariables(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        ListNode codeLocalsNode = (ListNode) codeNode.get(NodeConstants.LOCALS);
        if (codeLocalsNode != null) {
            for (Node n : codeLocalsNode) {
                MapNode localNode = (MapNode) n;
                String localName = ((ValueNode<String>) localNode.get(NodeConstants.NAME)).getValue();
                String localDesc = ((ValueNode<String>) localNode.get(NodeConstants.DESCRIPTOR)).getValue();
                
                ValueNode<String> localSignatureNode = (ValueNode<String>) localNode.get(NodeConstants.SIGNATURE);
                String localSignature = localSignatureNode == null? localDesc: localSignatureNode.getValue();
                
                String start = ((ValueNode<String>) localNode.get(NodeConstants.START)).getValue();
                String end = ((ValueNode<String>) localNode.get(NodeConstants.END)).getValue();
                int index = ((ValueNode<Integer>) localNode.get(NodeConstants.INDEX)).getValue().intValue();
    
                Label startLabel = labelMap.get(start);
                Label endLabel = labelMap.get(end);
    
                methodVisitor
                        .visitLocalVariable(localName, localDesc, localSignature, startLabel, endLabel, index);
    
                // visitLocalVariableAnnotation
                // TODO
            }
        }
    }

    private void visitTryCatchBlocks(MethodVisitor methodVisitor, MapNode codeNode, Map<String, Label> labelMap) {
        for (Node n : (ListNode) codeNode.get(NodeConstants.TRY_CATCH_BLOCKS)) {
            MapNode tryCatchBlock = (MapNode) n;

            String start = ((ValueNode<String>) tryCatchBlock.get(NodeConstants.START)).getValue();
            String end = ((ValueNode<String>) tryCatchBlock.get(NodeConstants.END)).getValue();
            String handler = ((ValueNode<String>) tryCatchBlock.get(NodeConstants.HANDLER)).getValue();
            String type = ((ValueNode<String>) tryCatchBlock.get(NodeConstants.TYPE)).getValue();

            Label startLabel = labelMap.computeIfAbsent(start, s -> new Label());
            Label endLabel = labelMap.computeIfAbsent(end, s -> new Label());
            Label handlerLabel = labelMap.computeIfAbsent(handler, s -> new Label());

            methodVisitor.visitTryCatchBlock(startLabel, endLabel, handlerLabel, type);

            // visitTryCatchBlockAnnotations
            for (Node n2 : (ListNode) tryCatchBlock.get(NodeConstants.ANNOTATIONS)) {
                ChasmAnnotationWriter writer = new ChasmAnnotationWriter(n2);
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
        ListNode methodAnnotationsNode = (ListNode) methodNode.get(NodeConstants.ANNOTATIONS);
        if (methodAnnotationsNode != null) {
            for (Node n : methodAnnotationsNode) {
                ChasmAnnotationWriter writer = new ChasmAnnotationWriter(n);
                writer.visitAnnotation(methodVisitor::visitAnnotation, methodVisitor::visitTypeAnnotation);
            }
        }
    }

    private void visitAnnotationDefault(MethodVisitor methodVisitor) {
        if (methodNode.containsKey(NodeConstants.ANNOTATION_DEFAULT)) {
            AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
            ChasmAnnotationWriter writer =
                    new ChasmAnnotationWriter(methodNode.get(NodeConstants.ANNOTATION_DEFAULT));
            writer.visitAnnotation(annotationVisitor);
        }
    }

    private void visitParameterAnnotations(MethodVisitor methodVisitor) {
        // visitParameterAnnotation
        int visibleCount = 0;
        int invisibleCount = 0;
        for (Node n : (ListNode) methodNode.get(NodeConstants.PARAMETER_ANNOTATIONS)) {
            MapNode annotationNode = (MapNode) n;
            int parameter = ((ValueNode<Integer>) annotationNode.get(NodeConstants.PARAMETER)).getValue().intValue();
            String annotationDesc = ((ValueNode<String>) annotationNode.get(NodeConstants.DESCRIPTOR)).getValue();
            
            ValueNode<Boolean> methodAnnotationVisibilityNode = (ValueNode<Boolean>) annotationNode.get(NodeConstants.VISIBLE);
            boolean visible = methodAnnotationVisibilityNode == null ||
                methodAnnotationVisibilityNode.getValue().booleanValue();

            ChasmAnnotationWriter writer = new ChasmAnnotationWriter(n);
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
        if (methodParametersNode != null) {
            for (Node n : methodParametersNode) {
                MapNode parameterNode = (MapNode) n;
                String parameterName = ((ValueNode<String>) parameterNode.get(NodeConstants.NAME)).getValue();
                int parameterAccess = ((ValueNode<Integer>) parameterNode.get(NodeConstants.ACCESS)).getValue().intValue();
                methodVisitor.visitParameter(parameterName, parameterAccess);
            }
        }
    }

    public void visitMethod(ClassVisitor visitor) {
        int access = ((ValueNode<Integer>) methodNode.get(NodeConstants.ACCESS)).getValue().intValue();
        String name = ((ValueNode<String>) methodNode.get(NodeConstants.NAME)).getValue();
        String descriptor = ((ValueNode<String>) methodNode.get(NodeConstants.DESCRIPTOR)).getValue();
        
        ValueNode<String> signatureNode = (ValueNode<String>) methodNode.get(NodeConstants.SIGNATURE);
        String signature = signatureNode == null? null: signatureNode.getValue();
        if (signature == null) {
            signature = descriptor;
        }
        ListNode exceptionsNode = (ListNode) methodNode.get(NodeConstants.EXCEPTIONS);
        String[] exceptions = exceptionsNode == null? new String[0]:
                exceptionsNode.stream().map(n -> ((ValueNode<String>) n).getValue()).toArray(String[]::new);

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
