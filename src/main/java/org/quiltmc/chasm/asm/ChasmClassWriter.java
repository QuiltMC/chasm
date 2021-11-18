package org.quiltmc.chasm.asm;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.RecordComponentVisitor;
import org.objectweb.asm.TypePath;
import org.quiltmc.chasm.LazyClassNode;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

@SuppressWarnings("unchecked")
public class ChasmClassWriter {
    private final MapNode classNode;

    public ChasmClassWriter(MapNode classNode) {
        this.classNode = classNode;
    }

    public void accept(ClassVisitor visitor) {
        // Unmodified class
        if (classNode instanceof LazyClassNode) {
            ((LazyClassNode) classNode).getClassReader().accept(visitor, 0);
        }

        // visit
        {
            int version = ((ValueNode<Integer>) classNode.get(NodeConstants.VERSION)).getValue();
            int access = ((ValueNode<Integer>) classNode.get(NodeConstants.ACCESS)).getValue();
            String name = ((ValueNode<String>) classNode.get(NodeConstants.NAME)).getValue();
            String signature = ((ValueNode<String>) classNode.get(NodeConstants.SIGNATURE)).getValue();
            String superClass = ((ValueNode<String>) classNode.get(NodeConstants.SUPER)).getValue();
            String[] interfaces = ((ListNode) classNode.get(NodeConstants.INTERFACES))
                    .stream().map(n -> ((ValueNode<String>) n).getValue()).toArray(String[]::new);

            visitor.visit(version, access, name, signature, superClass, interfaces);
        }

        // visitSource
        {
            String source = null;
            if (classNode.containsKey(NodeConstants.SOURCE)) {
                source = ((ValueNode<String>) classNode.get(NodeConstants.SOURCE)).getValue();
            }

            String debug = null;
            if (classNode.containsKey(NodeConstants.DEBUG)) {
                debug = ((ValueNode<String>) classNode.get(NodeConstants.DEBUG)).getValue();
            }

            visitor.visitSource(source, debug);
        }

        // visitModule
        if (classNode.containsKey(NodeConstants.MODULE)) {
            MapNode moduleNode = (MapNode) classNode.get(NodeConstants.MODULE);
            String name = ((ValueNode<String>) moduleNode.get(NodeConstants.NAME)).getValue();
            int access = ((ValueNode<Integer>) moduleNode.get(NodeConstants.ACCESS)).getValue();
            String version = ((ValueNode<String>) moduleNode.get(NodeConstants.VERSION)).getValue();

            ModuleVisitor moduleVisitor = visitor.visitModule(name, access, version);

            // visitMainClass
            if (moduleNode.containsKey(NodeConstants.MAIN)) {
                moduleVisitor.visitMainClass(((ValueNode<String>) moduleNode.get(NodeConstants.MAIN)).getValue());
            }

            // visitPackage
            for (Node n : (ListNode) moduleNode.get(NodeConstants.PACKAGES)) {
                moduleVisitor.visitPackage(((ValueNode<String>) n).getValue());
            }

            // visitRequire
            for (Node n : (ListNode) moduleNode.get(NodeConstants.REQUIRES)) {
                MapNode requireNode = (MapNode) n;
                String reqModule = ((ValueNode<String>) requireNode.get(NodeConstants.MODULE)).getValue();
                Integer reqAccess = ((ValueNode<Integer>) requireNode.get(NodeConstants.ACCESS)).getValue();
                String reqVersion = ((ValueNode<String>) requireNode.get(NodeConstants.VERSION)).getValue();
                moduleVisitor.visitRequire(reqModule, reqAccess, reqVersion);
            }

            // visitExport
            for (Node n : (ListNode) moduleNode.get(NodeConstants.EXPORTS)) {
                MapNode exportNode = (MapNode) n;
                String expPackage = ((ValueNode<String>) exportNode.get(NodeConstants.PACKAGE)).getValue();
                Integer expAcccess = ((ValueNode<Integer>) exportNode.get(NodeConstants.ACCESS)).getValue();
                ListNode reqModules = ((ListNode) exportNode.get(NodeConstants.MODULES));
                String[] modules = null;
                if (reqModules != null) {
                    modules = new String[reqModules.size()];
                    for (int i = 0; i < reqModules.size(); i++) {
                        modules[i] = ((ValueNode<String>) reqModules.get(i)).getValue();
                    }
                }
                moduleVisitor.visitExport(expPackage, expAcccess, modules);
            }

            // visitOpen
            for (Node n : (ListNode) moduleNode.get(NodeConstants.OPENS)) {
                MapNode openNode = (MapNode) n;
                String openPackage = ((ValueNode<String>) openNode.get(NodeConstants.PACKAGE)).getValue();
                Integer openAcccess = ((ValueNode<Integer>) openNode.get(NodeConstants.ACCESS)).getValue();
                ListNode openModules = ((ListNode) openNode.get(NodeConstants.MODULES));
                String[] modules = null;
                if (openModules != null) {
                    modules = new String[openModules.size()];
                    for (int i = 0; i < openModules.size(); i++) {
                        modules[i] = ((ValueNode<String>) openModules.get(i)).getValue();
                    }
                }
                moduleVisitor.visitOpen(openPackage, openAcccess, modules);
            }

            // visitUse
            for (Node n : (ListNode) moduleNode.get(NodeConstants.USES)) {
                moduleVisitor.visitUse(((ValueNode<String>) n).getValue());
            }

            // visitProvide
            for (Node n : (ListNode) moduleNode.get(NodeConstants.PROVIDES)) {
                MapNode providesNode = (MapNode) n;
                String service = ((ValueNode<String>) providesNode.get(NodeConstants.SERVICE)).getValue();
                ListNode providers = (ListNode) providesNode.get(NodeConstants.PROVIDERS);
                String[] prov = new String[providers.size()];
                for (int i = 0; i < providers.size(); i++) {
                    prov[i] = ((ValueNode<String>) providers.get(i)).getValue();
                }
                moduleVisitor.visitProvide(service, prov);
            }

            // visitEnd
            moduleVisitor.visitEnd();
        }

        // visitNestHost
        if (classNode.containsKey(NodeConstants.NEST_HOST)) {
            visitor.visitNestHost(((ValueNode<String>) classNode.get(NodeConstants.NEST_HOST)).getValue());
        }

        // visitOuterClass
        if (classNode.containsKey(NodeConstants.OWNER_CLASS)) {
            String ownerClass = ((ValueNode<String>) classNode.get(NodeConstants.OWNER_CLASS)).getValue();
            String ownerMethod = ((ValueNode<String>) classNode.get(NodeConstants.OWNER_METHOD)).getValue();
            String ownerDescriptor = ((ValueNode<String>) classNode.get(NodeConstants.OWNER_DESCRIPTOR)).getValue();
            visitor.visitOuterClass(ownerClass, ownerMethod, ownerDescriptor);
        }

        // visitAnnotation/visitTypeAnnotation
        for (Node n : (ListNode) classNode.get(NodeConstants.ANNOTATIONS)) {
            MapNode annotationNode = (MapNode) n;
            ValueNode<String> descriptor = (ValueNode<String>) annotationNode.get(NodeConstants.DESCRIPTOR);
            ValueNode<Boolean> visible = (ValueNode<Boolean>) annotationNode.get(NodeConstants.VISIBLE);
            ValueNode<Integer> typeRef = (ValueNode<Integer>) annotationNode.get(NodeConstants.TYPE_REF);
            ValueNode<String> typePath = (ValueNode<String>) annotationNode.get(NodeConstants.TYPE_PATH);

            if (typeRef == null) {
                AnnotationVisitor annotationVisitor =
                        visitor.visitAnnotation(descriptor.getValue(), visible.getValue());
                visitAnnotation(annotationVisitor, annotationNode);
            } else {
                AnnotationVisitor annotationVisitor =
                        visitor.visitTypeAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()),
                                descriptor.getValue(),
                                visible.getValue());
                visitAnnotation(annotationVisitor, annotationNode);
            }
        }

        // visitAttribute
        for (Node n : (ListNode) classNode.get(NodeConstants.ATTRIBUTES)) {
            visitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }

        // visitNestMember
        for (Node n : (ListNode) classNode.get(NodeConstants.NEST_MEMBERS)) {
            visitor.visitNestMember(((ValueNode<String>) n).getValue());
        }

        //visitPermittedSubclass
        for (Node n : (ListNode) classNode.get(NodeConstants.PERMITTED_SUBCLASSES)) {
            visitor.visitPermittedSubclass(((ValueNode<String>) n).getValue());
        }

        // visitInnerClass
        for (Node n : (ListNode) classNode.get(NodeConstants.INNER_CLASSES)) {
            MapNode innerClass = (MapNode) n;
            ValueNode<String> name = (ValueNode<String>) innerClass.get(NodeConstants.NAME);
            ValueNode<String> outerName = (ValueNode<String>) innerClass.get(NodeConstants.OUTER_NAME);
            ValueNode<String> innerName = (ValueNode<String>) innerClass.get(NodeConstants.INNER_NAME);
            ValueNode<Integer> access = (ValueNode<Integer>) innerClass.get(NodeConstants.ACCESS);

            visitor.visitInnerClass(name.getValue(), outerName.getValue(), innerName.getValue(), access.getValue());
        }

        // visitRecordComponent
        for (Node node : (ListNode) classNode.get(NodeConstants.RECORD_COMPONENTS)) {
            MapNode componentNode = (MapNode) node;
            String name = ((ValueNode<String>) componentNode.get(NodeConstants.NAME)).getValue();
            String descriptor = ((ValueNode<String>) componentNode.get(NodeConstants.DESCRIPTOR)).getValue();
            String signature = ((ValueNode<String>) componentNode.get(NodeConstants.SIGNATURE)).getValue();

            RecordComponentVisitor componentVisitor = visitor.visitRecordComponent(name, descriptor, signature);

            // visitAnnotation/visitTypeAnnotation
            for (Node n : (ListNode) componentNode.get(NodeConstants.ANNOTATIONS)) {
                MapNode annotationNode = (MapNode) n;
                ValueNode<String> annotationDesc = (ValueNode<String>) annotationNode.get(NodeConstants.DESCRIPTOR);
                ValueNode<Boolean> visible = (ValueNode<Boolean>) annotationNode.get(NodeConstants.VISIBLE);
                ValueNode<Integer> typeRef = (ValueNode<Integer>) annotationNode.get(NodeConstants.TYPE_REF);
                ValueNode<String> typePath = (ValueNode<String>) annotationNode.get(NodeConstants.TYPE_PATH);

                if (typeRef == null) {
                    AnnotationVisitor annotationVisitor =
                            componentVisitor.visitAnnotation(annotationDesc.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotationNode);
                } else {
                    AnnotationVisitor annotationVisitor =
                            componentVisitor.visitTypeAnnotation(typeRef.getValue(),
                                    TypePath.fromString(typePath.getValue()), annotationDesc.getValue(),
                                    visible.getValue());
                    visitAnnotation(annotationVisitor, annotationNode);
                }
            }

            // visitAttribute
            for (Node n : (ListNode) componentNode.get(NodeConstants.ATTRIBUTES)) {
                componentVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
            }

            // visitEnd
            componentVisitor.visitEnd();
        }

        // visitField
        for (Node node : (ListNode) classNode.get(NodeConstants.FIELDS)) {
            MapNode fieldNode = (MapNode) node;

            int access = ((ValueNode<Integer>) fieldNode.get(NodeConstants.ACCESS)).getValue();
            String name = ((ValueNode<String>) fieldNode.get(NodeConstants.NAME)).getValue();
            String descriptor = ((ValueNode<String>) fieldNode.get(NodeConstants.DESCRIPTOR)).getValue();
            String signature = ((ValueNode<String>) fieldNode.get(NodeConstants.SIGNATURE)).getValue();
            Object value = ((ValueNode<Object>) fieldNode.get(NodeConstants.VALUE)).getValue();

            FieldVisitor fieldVisitor = visitor.visitField(access, name, descriptor, signature, value);

            // visitAnnotation/visitTypeAnnotation
            for (Node n : (ListNode) fieldNode.get(NodeConstants.ANNOTATIONS)) {
                MapNode annotation = (MapNode) n;
                ValueNode<String> annotationDesc = (ValueNode<String>) annotation.get(NodeConstants.DESCRIPTOR);
                ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get(NodeConstants.VISIBLE);
                ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get(NodeConstants.TYPE_REF);
                ValueNode<String> typePath = (ValueNode<String>) annotation.get(NodeConstants.TYPE_PATH);
                if (typeRef == null) {
                    AnnotationVisitor annotationVisitor =
                            fieldVisitor.visitAnnotation(annotationDesc.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                } else {
                    AnnotationVisitor annotationVisitor =
                            fieldVisitor.visitTypeAnnotation(typeRef.getValue(),
                                    TypePath.fromString(typePath.getValue()), annotationDesc.getValue(),
                                    visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                }
            }

            // visitAttribute
            for (Node n : (ListNode) fieldNode.get(NodeConstants.ATTRIBUTES)) {
                fieldVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
            }

            // visitEnd
            fieldVisitor.visitEnd();
        }

        // visitMethod
        for (Node node : (ListNode) classNode.get(NodeConstants.METHODS)) {
            MapNode methodNode = (MapNode) node;

            int access = ((ValueNode<Integer>) methodNode.get(NodeConstants.ACCESS)).getValue();
            String name = ((ValueNode<String>) methodNode.get(NodeConstants.NAME)).getValue();
            String descriptor = ((ValueNode<String>) methodNode.get(NodeConstants.DESCRIPTOR)).getValue();
            String signature = ((ValueNode<String>) methodNode.get(NodeConstants.SIGNATURE)).getValue();
            String[] exceptions = ((ListNode) methodNode.get(NodeConstants.EXCEPTIONS))
                    .stream().map(n -> ((ValueNode<String>) n).getValue()).toArray(String[]::new);

            MethodVisitor methodVisitor = visitor.visitMethod(access, name, descriptor, signature, exceptions);

            // visitParameter
            for (Node n : (ListNode) methodNode.get(NodeConstants.PARAMETERS)) {
                MapNode parameterNode = (MapNode) n;
                String parameterName = ((ValueNode<String>) parameterNode.get(NodeConstants.NAME)).getValue();
                int parameterAccess = ((ValueNode<Integer>) parameterNode.get(NodeConstants.ACCESS)).getValue();
                methodVisitor.visitParameter(parameterName, parameterAccess);
            }

            // visitParameterAnnotation
            int visibleCount = 0;
            int invisibleCount = 0;
            for (Node n : (ListNode) methodNode.get(NodeConstants.PARAMETER_ANNOTATIONS)) {
                MapNode annotationNode = (MapNode) n;
                int parameter = ((ValueNode<Integer>) annotationNode.get(NodeConstants.PARAMETER)).getValue();
                String annotationDesc = ((ValueNode<String>) annotationNode.get(NodeConstants.DESCRIPTOR)).getValue();
                boolean visible = ((ValueNode<Boolean>) annotationNode.get(NodeConstants.VISIBLE)).getValue();

                AnnotationVisitor annotationVisitor =
                        methodVisitor.visitParameterAnnotation(parameter, annotationDesc, visible);
                visitAnnotation(annotationVisitor, annotationNode);

                if (visible) {
                    visibleCount++;
                } else {
                    invisibleCount++;
                }
            }

            // visitAnnotableParameterCount
            methodVisitor.visitAnnotableParameterCount(visibleCount, true);
            methodVisitor.visitAnnotableParameterCount(invisibleCount, false);

            // visitAnnotationDefault
            if (methodNode.containsKey(NodeConstants.ANNOTATION_DEFAULT)) {
                AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
                visitAnnotation(annotationVisitor, methodNode.get(NodeConstants.ANNOTATION_DEFAULT));
            }

            // visitAnnotation/visitTypeAnnotation
            for (Node n : (ListNode) methodNode.get(NodeConstants.ANNOTATIONS)) {
                MapNode annotation = (MapNode) n;
                ValueNode<String> annotationDesc = (ValueNode<String>) annotation.get(NodeConstants.DESCRIPTOR);
                ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get(NodeConstants.VISIBLE);
                ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get(NodeConstants.TYPE_REF);
                ValueNode<String> typePath = (ValueNode<String>) annotation.get(NodeConstants.TYPE_PATH);
                if (typeRef == null) {
                    AnnotationVisitor annotationVisitor =
                            methodVisitor.visitAnnotation(annotationDesc.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                } else {
                    AnnotationVisitor annotationVisitor =
                            methodVisitor.visitTypeAnnotation(typeRef.getValue(),
                                    TypePath.fromString(typePath.getValue()), annotationDesc.getValue(),
                                    visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                }
            }

            // visitAttribute
            for (Node n : (ListNode) methodNode.get(NodeConstants.ATTRIBUTES)) {
                methodVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
            }

            // visitCode
            if (methodNode.containsKey(NodeConstants.CODE)) {
                MapNode codeNode = (MapNode) methodNode.get(NodeConstants.CODE);
                Map<String, Label> labelMap = new HashMap<>();

                // visitFrame
                // Don't care

                // Instructions
                for (Node n : (ListNode) codeNode.get(NodeConstants.INSTRUCTIONS)) {
                    visitInstruction(methodVisitor, (MapNode) n, labelMap);
                }

                // visitTryCatchBlock
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
                        MapNode annotation = (MapNode) n2;

                        ValueNode<String> annotationDesc = (ValueNode<String>) annotation.get(NodeConstants.DESCRIPTOR);
                        ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get(NodeConstants.VISIBLE);
                        ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get(NodeConstants.TYPE_REF);
                        ValueNode<String> typePath = (ValueNode<String>) annotation.get(NodeConstants.TYPE_PATH);

                        AnnotationVisitor annotationVisitor =
                                methodVisitor.visitTryCatchAnnotation(typeRef.getValue(),
                                        TypePath.fromString(typePath.getValue()),
                                        annotationDesc.getValue(), visible.getValue());
                        visitAnnotation(annotationVisitor, annotation);
                    }
                }

                // visitLocalVariable
                for (Node n : (ListNode) codeNode.get(NodeConstants.LOCALS)) {
                    MapNode localNode = (MapNode) n;
                    String localName = ((ValueNode<String>) localNode.get(NodeConstants.NAME)).getValue();
                    String localDesc = ((ValueNode<String>) localNode.get(NodeConstants.DESCRIPTOR)).getValue();
                    String localSignature = ((ValueNode<String>) localNode.get(NodeConstants.SIGNATURE)).getValue();
                    String start = ((ValueNode<String>) localNode.get(NodeConstants.START)).getValue();
                    String end = ((ValueNode<String>) localNode.get(NodeConstants.END)).getValue();
                    int index = ((ValueNode<Integer>) localNode.get(NodeConstants.INDEX)).getValue();

                    Label startLabel = labelMap.get(start);
                    Label endLabel = labelMap.get(end);

                    methodVisitor.visitLocalVariable(localName, localDesc, localSignature, startLabel, endLabel, index);

                    // visitLocalVariableAnnotation
                    // TODO
                }

                // visitLineNumber
                // Don't care

                // visitMaxs
                methodVisitor.visitMaxs(0, 0);
            }

            // visitEnd
            methodVisitor.visitEnd();
        }

        // visitEnd
        visitor.visitEnd();
    }

    @SuppressWarnings("ConstantConditions")
    private void visitAnnotation(AnnotationVisitor visitor, Node annotationNode) {
        ListNode values;
        if (annotationNode instanceof MapNode) {
            values = (ListNode) ((MapNode) annotationNode).get(NodeConstants.VALUES);
        } else {
            values = (ListNode) annotationNode;
        }

        for (Node value : values) {
            String name = null;
            if (value instanceof MapNode mapNode && mapNode.containsKey(NodeConstants.NAME)) {
                // Name-value pairs
                name = ((ValueNode<String>) mapNode.get(NodeConstants.NAME)).getValue();
                value = mapNode.get(NodeConstants.VALUE);
            }

            if (value instanceof ValueNode) {
                visitor.visit(name, ((ValueNode<Object>) value).getValue());
            } else if (value instanceof ListNode) {
                AnnotationVisitor arrayVisitor = visitor.visitArray(name);
                visitAnnotation(arrayVisitor, value);
            } else {
                MapNode mapNode = (MapNode) value;
                if (mapNode.containsKey(NodeConstants.VALUE)) {
                    String descriptor = ((ValueNode<String>) mapNode.get(NodeConstants.DESCRIPTOR)).getValue();
                    String enumValue = ((ValueNode<String>) mapNode.get(NodeConstants.VALUE)).getValue();

                    visitor.visitEnum(name, descriptor, enumValue);
                } else {
                    String descriptor = ((ValueNode<String>) mapNode.get(NodeConstants.DESCRIPTOR)).getValue();
                    ListNode annotationvalues = (ListNode) mapNode.get(NodeConstants.VALUES);

                    AnnotationVisitor annotationVisitor = visitor.visitAnnotation(name, descriptor);
                    visitAnnotation(annotationVisitor, annotationvalues);
                }
            }
        }

        visitor.visitEnd();
    }

    private void visitInstruction(MethodVisitor visitor, MapNode instructionNode, Map<String, Label> labelMap) {
        // visitLabel
        ListNode labelsNode = (ListNode) instructionNode.get(NodeConstants.LABELS);
        for (Node n2 : labelsNode) {
            visitor.visitLabel(labelMap.computeIfAbsent(((ValueNode<String>) n2).getValue(), s -> new Label()));
        }

        if (instructionNode.containsKey(NodeConstants.LINE)) {
            if (labelsNode.isEmpty()) {
                throw new RuntimeException("Encountered line number without label.");
            }
            int line = ((ValueNode<Integer>) instructionNode.get(NodeConstants.LINE)).getValue();
            visitor.visitLineNumber(line, labelMap.get(((ValueNode<String>) labelsNode.get(0)).getValue()));
        }

        // visit<...>Insn
        int opcode = ((ValueNode<Integer>) instructionNode.get(NodeConstants.OPCODE)).getValue();
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
                visitor.visitInsn(opcode);
                break;
            }
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
            case Opcodes.NEWARRAY: {
                // visitIntInsn
                int operand = ((ValueNode<Integer>) instructionNode.get(NodeConstants.OPERAND)).getValue();
                visitor.visitIntInsn(opcode, operand);
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
                int var = ((ValueNode<Integer>) instructionNode.get(NodeConstants.VAR)).getValue();
                visitor.visitVarInsn(opcode, var);
                break;
            }
            case Opcodes.NEW:
            case Opcodes.ANEWARRAY:
            case Opcodes.CHECKCAST:
            case Opcodes.INSTANCEOF: {
                // visitTypeInsn
                String type = ((ValueNode<String>) instructionNode.get(NodeConstants.TYPE)).getValue();
                visitor.visitTypeInsn(opcode, type);
                break;
            }
            case Opcodes.GETSTATIC:
            case Opcodes.PUTSTATIC:
            case Opcodes.GETFIELD:
            case Opcodes.PUTFIELD: {
                // visitFieldInsn
                String owner = ((ValueNode<String>) instructionNode.get(NodeConstants.OWNER)).getValue();
                String name = ((ValueNode<String>) instructionNode.get(NodeConstants.NAME)).getValue();
                String descriptor = ((ValueNode<String>) instructionNode.get(NodeConstants.DESCRIPTOR)).getValue();
                visitor.visitFieldInsn(opcode, owner, name, descriptor);
                break;
            }
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE: {
                // visitMethodInsns
                String owner = ((ValueNode<String>) instructionNode.get(NodeConstants.OWNER)).getValue();
                String name = ((ValueNode<String>) instructionNode.get(NodeConstants.NAME)).getValue();
                String descriptor = ((ValueNode<String>) instructionNode.get(NodeConstants.DESCRIPTOR)).getValue();
                Boolean isInterface = ((ValueNode<Boolean>) instructionNode.get(NodeConstants.IS_INTERFACE)).getValue();
                visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                break;
            }
            case Opcodes.INVOKEDYNAMIC: {
                // visitInvokeDynamicInsn
                String name = ((ValueNode<String>) instructionNode.get(NodeConstants.NAME)).getValue();
                String descriptor = ((ValueNode<String>) instructionNode.get(NodeConstants.DESCRIPTOR)).getValue();
                Handle handle = getHandle((MapNode) instructionNode.get(NodeConstants.HANDLE));
                Object[] arguments = getArguments((ListNode) instructionNode.get(NodeConstants.ARGUMENTS));
                visitor.visitInvokeDynamicInsn(name, descriptor, handle, arguments);
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
                String labelString = ((ValueNode<String>) instructionNode.get(NodeConstants.TARGET)).getValue();
                Label label = labelMap.computeIfAbsent(labelString, s -> new Label());
                visitor.visitJumpInsn(opcode, label);
                break;
            }
            case Opcodes.LDC: {
                // visitLdcInsn
                Object value = ((ValueNode<Object>) instructionNode.get(NodeConstants.VALUE)).getValue();
                visitor.visitLdcInsn(value);
                break;
            }
            case Opcodes.IINC: {
                // visitIincInsn
                int var = ((ValueNode<Integer>) instructionNode.get(NodeConstants.VAR)).getValue();
                int increment = ((ValueNode<Integer>) instructionNode.get(NodeConstants.INCREMENT)).getValue();
                visitor.visitIincInsn(var, increment);
                break;
            }
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH: {
                // visitTableSwitchInsn / visitLookupSwitchInsn
                String defaultString = ((ValueNode<String>) instructionNode.get(NodeConstants.DEFAULT)).getValue();
                Label dflt = labelMap.computeIfAbsent(defaultString, s -> new Label());
                ListNode cases = (ListNode) instructionNode.get(NodeConstants.CASES);
                int[] keys = new int[cases.size()];
                Label[] labels = new Label[cases.size()];
                for (int i = 0; i < cases.size(); i++) {
                    MapNode caseNode = (MapNode) cases.get(i);
                    keys[i] = ((ValueNode<Integer>) caseNode.get(NodeConstants.KEY)).getValue();
                    String caseLabelString = ((ValueNode<String>) caseNode.get(NodeConstants.LABEL)).getValue();
                    labels[i] = labelMap.computeIfAbsent(caseLabelString, s -> new Label());
                }

                if (opcode == Opcodes.LOOKUPSWITCH) {
                    visitor.visitLookupSwitchInsn(dflt, keys, labels);
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
                        visitor.visitTableSwitchInsn(keys[0], keys[0] + keys.length - 1, dflt, labels);
                    } else {
                        visitor.visitLookupSwitchInsn(dflt, keys, labels);
                    }
                }
                break;
            }
            case Opcodes.MULTIANEWARRAY: {
                // visitMultiANewArrayInsn
                String descriptor = ((ValueNode<String>) instructionNode.get(NodeConstants.DESCRIPTOR)).getValue();
                int dimensions = ((ValueNode<Integer>) instructionNode.get(NodeConstants.DIMENSIONS)).getValue();
                visitor.visitMultiANewArrayInsn(descriptor, dimensions);
                break;
            }
            default:
                throw new RuntimeException("Unknown instruction opcode");
        }

        // visitInsnAnnotation
        for (Node n : (ListNode) instructionNode.get(NodeConstants.ANNOTATIONS)) {
            MapNode annotation = (MapNode) n;

            ValueNode<String> annotationDesc = (ValueNode<String>) annotation.get(NodeConstants.DESCRIPTOR);
            ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get(NodeConstants.VISIBLE);
            ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get(NodeConstants.TYPE_REF);
            ValueNode<String> typePath = (ValueNode<String>) annotation.get(NodeConstants.TYPE_PATH);

            AnnotationVisitor annotationVisitor =
                    visitor.visitTypeAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()),
                            annotationDesc.getValue(),
                            visible.getValue());
            visitAnnotation(annotationVisitor, annotation);
        }
    }

    private Object[] getArguments(ListNode argumentNode) {
        Object[] arguments = new Object[argumentNode.size()];
        for (int i = 0; i < arguments.length; i++) {
            Node argNode = argumentNode.get(i);
            if (argNode instanceof ValueNode valueNode) {
                arguments[i] = valueNode.getValue();
            } else if (((MapNode) argNode).containsKey(NodeConstants.TAG)) {
                arguments[i] = getHandle((MapNode) argNode);
            } else {
                MapNode constDynamicNode = (MapNode) argNode;
                String name = ((ValueNode<String>) constDynamicNode.get(NodeConstants.NAME)).getValue();
                String descriptor = ((ValueNode<String>) constDynamicNode.get(NodeConstants.DESCRIPTOR)).getValue();
                Handle handle = getHandle((MapNode) constDynamicNode.get(NodeConstants.HANDLE));
                Object[] args = getArguments((ListNode) constDynamicNode.get(NodeConstants.ARGS));
                arguments[i] = new ConstantDynamic(name, descriptor, handle, args);
            }
        }

        return arguments;
    }

    private Handle getHandle(MapNode handleNode) {
        int tag = ((ValueNode<Integer>) handleNode.get(NodeConstants.TAG)).getValue();
        String owner = ((ValueNode<String>) handleNode.get(NodeConstants.OWNER)).getValue();
        String name = ((ValueNode<String>) handleNode.get(NodeConstants.NAME)).getValue();
        String descriptor = ((ValueNode<String>) handleNode.get(NodeConstants.DESCRIPTOR)).getValue();
        boolean isInterface = ((ValueNode<Boolean>) handleNode.get(NodeConstants.IS_INTERFACE)).getValue();

        return new Handle(tag, owner, name, descriptor, isInterface);
    }
}
