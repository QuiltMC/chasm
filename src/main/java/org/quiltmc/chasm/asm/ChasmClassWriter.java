package org.quiltmc.chasm.asm;

import org.objectweb.asm.*;
import org.quiltmc.chasm.LazyClassNode;
import org.quiltmc.chasm.tree.*;

import java.util.HashMap;
import java.util.Map;

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
            int version = ((ValueNode<Integer>) classNode.get("version")).getValue();
            int access = ((ValueNode<Integer>) classNode.get("access")).getValue();
            String name = ((ValueNode<String>) classNode.get("name")).getValue();
            String signature = ((ValueNode<String>) classNode.get("signature")).getValue();
            String superClass = ((ValueNode<String>) classNode.get("super")).getValue();
            String[] interfaces = ((ListNode) classNode.get("interfaces"))
                    .stream().map(n -> ((ValueNode<String>) n).getValue()).toArray(String[]::new);

            visitor.visit(version, access, name, signature, superClass, interfaces);
        }

        // visitSource
        {
            // Don't care
        }

        // visitModule
        if (classNode.containsKey("module")) {
            MapNode moduleNode = (MapNode) classNode.get("module");
            String name = ((ValueNode<String>) moduleNode.get("name")).getValue();
            int access = ((ValueNode<Integer>) moduleNode.get("access")).getValue();
            String version = ((ValueNode<String>) moduleNode.get("version")).getValue();

            ModuleVisitor moduleVisitor = visitor.visitModule(name, access, version);

            // visitMainClass
            if (moduleNode.containsKey("main")) {
                moduleVisitor.visitMainClass(((ValueNode<String>)moduleNode.get("main")).getValue());
            }

            // visitPackage
            for (Node n : (ListNode)moduleNode.get("packages")) {
                moduleVisitor.visitPackage(((ValueNode<String>) n).getValue());
            }

            // visitRequire
            for (Node n : (ListNode)moduleNode.get("requires")) {
                MapNode requireNode = (MapNode) n;
                String reqModule = ((ValueNode<String>) requireNode.get("module")).getValue();
                Integer reqAccess = ((ValueNode<Integer>) requireNode.get("access")).getValue();
                String reqVersion = ((ValueNode<String>) requireNode.get("version")).getValue();
                moduleVisitor.visitRequire(reqModule, reqAccess, reqVersion);
            }

            // visitExport
            for (Node n : (ListNode)moduleNode.get("exports")) {
                MapNode exportNode = (MapNode) n;
                String expPackage = ((ValueNode<String>) exportNode.get("package")).getValue();
                Integer expAcccess = ((ValueNode<Integer>) exportNode.get("access")).getValue();
                ListNode reqModules = ((ListNode) exportNode.get("modules"));
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
            for (Node n : (ListNode)moduleNode.get("opens")) {
                MapNode openNode = (MapNode) n;
                String openPackage = ((ValueNode<String>) openNode.get("package")).getValue();
                Integer openAcccess = ((ValueNode<Integer>) openNode.get("access")).getValue();
                ListNode openModules = ((ListNode) openNode.get("modules"));
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
            for (Node n : (ListNode)moduleNode.get("uses")) {
                moduleVisitor.visitUse(((ValueNode<String>) n).getValue());
            }

            // visitProvide
            for (Node n : (ListNode)moduleNode.get("provides")) {
                MapNode providesNode = (MapNode) n;
                String service = ((ValueNode<String>) providesNode.get("service")).getValue();
                ListNode providers = (ListNode) providesNode.get("providers");
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
        if (classNode.containsKey("nestHost")) {
            visitor.visitNestHost(((ValueNode<String>) classNode.get("nestHost")).getValue());
        }

        // visitOuterClass
        if (classNode.containsKey("ownerClass")) {
            String ownerClass = ((ValueNode<String>) classNode.get("ownerClass")).getValue();
            String ownerMethod = ((ValueNode<String>) classNode.get("ownerMethod")).getValue();
            String ownerDescriptor = ((ValueNode<String>) classNode.get("ownerDescriptor")).getValue();
            visitor.visitOuterClass(ownerClass, ownerMethod, ownerDescriptor);
        }

        // visitAnnotation/visitTypeAnnotation
        for (Node n : (ListNode) classNode.get("annotations")) {
            MapNode annotationNode = (MapNode) n;
            ValueNode<String> descriptor = (ValueNode<String>) annotationNode.get("descriptor");
            ValueNode<Boolean> visible = (ValueNode<Boolean>) annotationNode.get("visible");
            ValueNode<Integer> typeRef = (ValueNode<Integer>) annotationNode.get("typeRef");
            ValueNode<String> typePath = (ValueNode<String>) annotationNode.get("typePath");

            if (typeRef == null) {
                AnnotationVisitor annotationVisitor = visitor.visitAnnotation(descriptor.getValue(), visible.getValue());
                visitAnnotation(annotationVisitor, annotationNode);
            }
            else {
                AnnotationVisitor annotationVisitor = visitor.visitTypeAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()), descriptor.getValue(), visible.getValue());
                visitAnnotation(annotationVisitor, annotationNode);
            }
        }

        // visitAttribute
        for (Node n : (ListNode) classNode.get("attributes")) {
           visitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
        }

        // visitNestMember
        for (Node n : (ListNode) classNode.get("nestMembers")) {
            visitor.visitNestMember(((ValueNode<String>) n).getValue());
        }

        //visitPermittedSubclass
        for (Node n : (ListNode) classNode.get("permittedSubclasses")) {
            visitor.visitPermittedSubclass(((ValueNode<String>) n).getValue());
        }

        // visitInnerClass
        for (Node n : (ListNode) classNode.get("permittedSubclasses")) {
            MapNode innerClass = (MapNode) n;
            ValueNode<String> name = (ValueNode<String>) innerClass.get("name");
            ValueNode<String> outerName = (ValueNode<String>) innerClass.get("outerName");
            ValueNode<String> innerName = (ValueNode<String>) innerClass.get("innerName");
            ValueNode<Integer> access = (ValueNode<Integer>) innerClass.get("access");

            visitor.visitInnerClass(name.getValue(), outerName.getValue(), innerName.getValue(), access.getValue());
        }

        // visitRecordComponent
        for (Node node : (ListNode) classNode.get("recordComponents")) {
            MapNode componentNode = (MapNode) node;
            String name = ((ValueNode<String>) componentNode.get("name")).getValue();
            String descriptor = ((ValueNode<String>) componentNode.get("descriptor")).getValue();
            String signature = ((ValueNode<String>) componentNode.get("signature")).getValue();

            RecordComponentVisitor componentVisitor = visitor.visitRecordComponent(name, descriptor, signature);

            // visitAnnotation/visitTypeAnnotation
            for (Node n : (ListNode) componentNode.get("annotations")) {
                MapNode annotationNode = (MapNode) n;
                ValueNode<String> aDescriptor = (ValueNode<String>) annotationNode.get("descriptor");
                ValueNode<Boolean> visible = (ValueNode<Boolean>) annotationNode.get("visible");
                ValueNode<Integer> typeRef = (ValueNode<Integer>) annotationNode.get("typeRef");
                ValueNode<String> typePath = (ValueNode<String>) annotationNode.get("typePath");

                if (typeRef == null) {
                    AnnotationVisitor annotationVisitor = componentVisitor.visitAnnotation(aDescriptor.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotationNode);
                }
                else {
                    AnnotationVisitor annotationVisitor = componentVisitor.visitTypeAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()), aDescriptor.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotationNode);
                }
            }

            // visitAttribute
            for (Node n : (ListNode) componentNode.get("attributes")) {
                componentVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
            }

            // visitEnd
            componentVisitor.visitEnd();
        }

        // visitField
        for (Node node : (ListNode) classNode.get("fields")) {
            MapNode fieldNode = (MapNode) node;

            int access = ((ValueNode<Integer>) fieldNode.get("access")).getValue();
            String name = ((ValueNode<String>) fieldNode.get("name")).getValue();
            String descriptor = ((ValueNode<String>) fieldNode.get("descriptor")).getValue();
            String signature = ((ValueNode<String>) fieldNode.get("signature")).getValue();
            Object value = ((ValueNode<Object>) fieldNode.get("value")).getValue();

            FieldVisitor fieldVisitor = visitor.visitField(access, name, descriptor, signature, value);

            // visitAnnotation/visitTypeAnnotation
            for (Node n : (ListNode) fieldNode.get("annotations")) {
                MapNode annotation = (MapNode) n;
                ValueNode<String> aDescriptor = (ValueNode<String>) annotation.get("descriptor");
                ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get("visible");
                ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get("typeRef");
                ValueNode<String> typePath = (ValueNode<String>) annotation.get("typePath");
                if (typeRef == null) {
                    AnnotationVisitor annotationVisitor = fieldVisitor.visitAnnotation(aDescriptor.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                }
                else {
                    AnnotationVisitor annotationVisitor = fieldVisitor.visitTypeAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()), aDescriptor.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                }
            }

            // visitAttribute
            for (Node n : (ListNode) fieldNode.get("attributes")) {
                fieldVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
            }

            // visitEnd
            fieldVisitor.visitEnd();
        }

        // visitMethod
        for (Node node : (ListNode) classNode.get("methods")) {
            MapNode methodNode = (MapNode) node;

            int access = ((ValueNode<Integer>) methodNode.get("access")).getValue();
            String name = ((ValueNode<String>) methodNode.get("name")).getValue();
            String descriptor = ((ValueNode<String>) methodNode.get("descriptor")).getValue();
            String signature = ((ValueNode<String>) methodNode.get("signature")).getValue();
            String[] exceptions = ((ListNode) methodNode.get("exceptions"))
                    .stream().map(n -> ((ValueNode<String>) n).getValue()).toArray(String[]::new);

            MethodVisitor methodVisitor = visitor.visitMethod(access, name, descriptor, signature, exceptions);

            // visitParameter
            for (Node n : (ListNode) methodNode.get("parameters")) {
                MapNode parameterNode = (MapNode) n;
                String parameterName = ((ValueNode<String>) parameterNode.get("name")).getValue();
                int parameterAccess = ((ValueNode<Integer>) parameterNode.get("access")).getValue();
                methodVisitor.visitParameter(parameterName, parameterAccess);
            }

            // visitParameterAnnotation
            int visibleCount = 0;
            int invisibleCount = 0;
            for (Node n : (ListNode) methodNode.get("parameterAnnotations")) {
                MapNode annotationNode = (MapNode) n;
                int parameter = ((ValueNode<Integer>)annotationNode.get("parameter")).getValue();
                String aDescriptor = ((ValueNode<String>)annotationNode.get("descriptor")).getValue();
                boolean visible = ((ValueNode<Boolean>)annotationNode.get("visible")).getValue();

                AnnotationVisitor annotationVisitor = methodVisitor.visitParameterAnnotation(parameter, aDescriptor, visible);
                visitAnnotation(annotationVisitor, annotationNode);

                if (visible) {
                    visibleCount++;
                }
                else {
                    invisibleCount++;
                }
            }

            // visitAnnotableParameterCount
            methodVisitor.visitAnnotableParameterCount(visibleCount, true);
            methodVisitor.visitAnnotableParameterCount(invisibleCount, false);

            // visitAnnotationDefault
            if (methodNode.containsKey("annotationDefault")) {
                AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
                visitAnnotation(annotationVisitor, (MapNode) methodNode.get("annotationDefault"));
            }

            // visitAnnotation/visitTypeAnnotation
            for (Node n : (ListNode) methodNode.get("annotations")) {
                MapNode annotation = (MapNode) n;
                ValueNode<String> aDescriptor = (ValueNode<String>) annotation.get("descriptor");
                ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get("visible");
                ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get("typeRef");
                ValueNode<String> typePath = (ValueNode<String>) annotation.get("typePath");
                if (typeRef == null) {
                    AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation(aDescriptor.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                }
                else {
                    AnnotationVisitor annotationVisitor = methodVisitor.visitTypeAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()), aDescriptor.getValue(), visible.getValue());
                    visitAnnotation(annotationVisitor, annotation);
                }
            }

            // visitAttribute
            for (Node n : (ListNode) methodNode.get("attributes")) {
                methodVisitor.visitAttribute(((ValueNode<Attribute>) n).getValue());
            }

            // visitCode
            if (methodNode.containsKey("code")) {
                MapNode codeNode = (MapNode) methodNode.get("code");
                Map<String, Label> labelMap = new HashMap<>();

                // visitFrame
                // Don't care

                // Instructions
                for (Node n : (ListNode) codeNode.get("instructions")) {
                    visitInstruction(methodVisitor, (MapNode) n, labelMap);
                }

                // visitTryCatchBlock
                for (Node n : (ListNode) codeNode.get("tryCatchBlocks")) {
                    MapNode tryCatchBlock = (MapNode) n;

                    String start = ((ValueNode<String>) tryCatchBlock.get("start")).getValue();
                    String end = ((ValueNode<String>) tryCatchBlock.get("end")).getValue();
                    String handler = ((ValueNode<String>) tryCatchBlock.get("handler")).getValue();
                    String type = ((ValueNode<String>) tryCatchBlock.get("type")).getValue();

                    Label startLabel = labelMap.computeIfAbsent(start, s -> new Label());
                    Label endLabel = labelMap.computeIfAbsent(end, s -> new Label());
                    Label handlerLabel = labelMap.computeIfAbsent(handler, s -> new Label());

                    methodVisitor.visitTryCatchBlock(startLabel, endLabel, handlerLabel, type);

                    // visitTryCatchBlockAnnotations
                    for (Node n2 : (ListNode) tryCatchBlock.get("annotations")) {
                        MapNode annotation = (MapNode) n2;

                        ValueNode<String> aDescriptor = (ValueNode<String>) annotation.get("descriptor");
                        ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get("visible");
                        ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get("typeRef");
                        ValueNode<String> typePath = (ValueNode<String>) annotation.get("typePath");

                        AnnotationVisitor annotationVisitor = methodVisitor.visitTryCatchAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()), aDescriptor.getValue(), visible.getValue());
                        visitAnnotation(annotationVisitor, annotation);
                    }
                }

                // visitLocalVariable
                for (Node n : (ListNode) codeNode.get("locals")) {
                    MapNode localNode = (MapNode) n;
                    String lName = ((ValueNode<String>) localNode.get("name")).getValue();
                    String lDescriptor = ((ValueNode<String>) localNode.get("descriptor")).getValue();
                    String lSignature = ((ValueNode<String>) localNode.get("signature")).getValue();
                    String start = ((ValueNode<String>) localNode.get("start")).getValue();
                    String end = ((ValueNode<String>) localNode.get("end")).getValue();
                    int index = ((ValueNode<Integer>) localNode.get("index")).getValue();

                    Label startLabel = labelMap.get(start);
                    Label endLabel = labelMap.get(end);

                    methodVisitor.visitLocalVariable(lName, lDescriptor, lSignature, startLabel, endLabel, index);

                    // visitLocalVariableAnnotation
                    // TODO
                }

                // visitLineNumber
                // Don't care

                // visitMaxs
                // Don't care
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
            values = (ListNode) ((MapNode) annotationNode).get("values");
        }
        else {
            values = (ListNode) annotationNode;
        }

        for (Node value : values) {
            String name = null;
            if (value instanceof MapNode mapNode && mapNode.containsKey("name")) {
                // Name-value pairs
                name = ((ValueNode<String>) mapNode.get("name")).getValue();
                value = mapNode.get("value");
            }

            if (value instanceof ValueNode) {
                visitor.visit(name, value);
            }
            else if (value instanceof ListNode) {
                AnnotationVisitor arrayVisitor = visitor.visitArray(name);
                visitAnnotation(arrayVisitor, value);
            }
            else {
                MapNode mapNode = (MapNode) value;
                if (mapNode.containsKey("value")) {
                    String descriptor = ((ValueNode<String>) mapNode.get("descriptor")).getValue();
                    String enumValue = ((ValueNode<String>) mapNode.get("value")).getValue();

                    visitor.visitEnum(name, descriptor, enumValue);
                }
                else {
                    String descriptor = ((ValueNode<String>) mapNode.get("descriptor")).getValue();
                    ListNode annotationvalues = (ListNode) mapNode.get("values");

                    AnnotationVisitor annotationVisitor = visitor.visitAnnotation(name, descriptor);
                    visitAnnotation(annotationVisitor, annotationvalues);
                }
            }
        }

        visitor.visitEnd();
    }

    private void visitInstruction(MethodVisitor visitor, MapNode instructionNode, Map<String, Label> labelMap) {
        // visitLabel
        for (Node n2 : (ListNode) instructionNode.get("labels")) {
            visitor.visitLabel(labelMap.computeIfAbsent(((ValueNode<String>) n2).getValue(), s -> new Label()));
        }

        // visit<...>Insn
        int opcode = ((ValueNode<Integer>)instructionNode.get("opcode")).getValue();
        switch (opcode) {
            case Opcodes.NOP:
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
                int operand = ((ValueNode<Integer>) instructionNode.get("operand")).getValue();
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
                int var = ((ValueNode<Integer>) instructionNode.get("var")).getValue();
                visitor.visitVarInsn(opcode, var);
                break;
            }
            case Opcodes.NEW:
            case Opcodes.ANEWARRAY:
            case Opcodes.CHECKCAST:
            case Opcodes.INSTANCEOF: {
                // visitTypeInsn
                String type = ((ValueNode<String>) instructionNode.get("type")).getValue();
                visitor.visitTypeInsn(opcode, type);
                break;
            }
            case Opcodes.GETSTATIC:
            case Opcodes.PUTSTATIC:
            case Opcodes.GETFIELD:
            case Opcodes.PUTFIELD: {
                // visitFieldInsn
                String owner = ((ValueNode<String>) instructionNode.get("owner")).getValue();
                String name = ((ValueNode<String>) instructionNode.get("name")).getValue();
                String descriptor = ((ValueNode<String>) instructionNode.get("descriptor")).getValue();
                visitor.visitFieldInsn(opcode, owner, name, descriptor);
                break;
            }
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE: {
                // visitMethodInsns
                String owner = ((ValueNode<String>) instructionNode.get("owner")).getValue();
                String name = ((ValueNode<String>) instructionNode.get("name")).getValue();
                String descriptor = ((ValueNode<String>) instructionNode.get("descriptor")).getValue();
                Boolean isInterface = ((ValueNode<Boolean>) instructionNode.get("isInterface")).getValue();
                visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
                break;
            }
            case Opcodes.INVOKEDYNAMIC: {
                // visitInvokeDynamicInsn
                String name = ((ValueNode<String>) instructionNode.get("name")).getValue();
                String descriptor = ((ValueNode<String>) instructionNode.get("descriptor")).getValue();
                Handle handle = getHandle((MapNode) instructionNode.get("handle"));
                Object[] arguments = getArguments((ListNode) instructionNode.get("arguments"));
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
                String labelString = ((ValueNode<String>) instructionNode.get("target")).getValue();
                Label label = labelMap.computeIfAbsent(labelString, s -> new Label());
                visitor.visitJumpInsn(opcode, label);
                break;
            }
            case Opcodes.LDC: {
                // visitLdcInsn
                Object value = ((ValueNode<Object>) instructionNode.get("value")).getValue();
                visitor.visitLdcInsn(value);
                break;
            }
            case Opcodes.IINC: {
                // visitIincInsn
                int var = ((ValueNode<Integer>) instructionNode.get("var")).getValue();
                int increment = ((ValueNode<Integer>) instructionNode.get("increment")).getValue();
                visitor.visitIincInsn(var, increment);
                break;
            }
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH: {
                // visitTableSwitchInsn / visitLookupSwitchInsn
                String defaultString = ((ValueNode<String>) instructionNode.get("default")).getValue();
                Label dflt = labelMap.computeIfAbsent(defaultString, s -> new Label());
                ListNode cases = (ListNode) instructionNode.get("cases");
                int[] keys = new int[cases.size()];
                Label[] labels = new Label[cases.size()];
                for (int i = 0; i < cases.size(); i++) {
                    MapNode caseNode = (MapNode) cases.get(i);
                    keys[i] = ((ValueNode<Integer>) caseNode.get("key")).getValue();
                    String caseLabelString = ((ValueNode<String>) caseNode.get("label")).getValue();
                    labels[i] = labelMap.computeIfAbsent(caseLabelString, s -> new Label());
                }

                // Check if switch can be a table switch
                boolean isTable = true;
                for (int i = 0; i < keys.length; i++) {
                    if (keys[i] != keys[0] + i) {
                        isTable = false;
                        break;
                    }
                }

                if (isTable) {
                    visitor.visitTableSwitchInsn(keys[0], keys[0] + keys.length - 1, dflt, labels);
                }
                else {
                    visitor.visitLookupSwitchInsn(dflt, keys, labels);
                }
                break;
            }
            case Opcodes.MULTIANEWARRAY: {
                // visitMultiANewArrayInsn
                String descriptor = ((ValueNode<String>) instructionNode.get("descriptor")).getValue();
                int dimensions = ((ValueNode<Integer>) instructionNode.get("dimensions")).getValue();
                visitor.visitMultiANewArrayInsn(descriptor, dimensions);
                break;
            }
            default:
                throw new RuntimeException("Unknown instruction opcode");
        }

        // visitInsnAnnotation
        for (Node n : (ListNode) instructionNode.get("annotations")) {
            MapNode annotation = (MapNode) n;

            ValueNode<String> aDescriptor = (ValueNode<String>) annotation.get("descriptor");
            ValueNode<Boolean> visible = (ValueNode<Boolean>) annotation.get("visible");
            ValueNode<Integer> typeRef = (ValueNode<Integer>) annotation.get("typeRef");
            ValueNode<String> typePath = (ValueNode<String>) annotation.get("typePath");

            AnnotationVisitor annotationVisitor = visitor.visitTypeAnnotation(typeRef.getValue(), TypePath.fromString(typePath.getValue()), aDescriptor.getValue(), visible.getValue());
            visitAnnotation(annotationVisitor, annotation);
        }
    }

    private Object[] getArguments(ListNode argumentNode) {
        Object[] arguments = new Object[argumentNode.size()];
        for (int i = 0; i < arguments.length; i++) {
            Node argNode = argumentNode.get(i);
            if (argNode instanceof ValueNode valueNode) {
                arguments[i] = valueNode.getValue();
            }
            else if (((MapNode) argNode).containsKey("tag")) {
                arguments[i] = getHandle((MapNode) argNode);
            }
            else {
                MapNode constDynamicNode = (MapNode) argNode;
                String name = ((ValueNode<String>) constDynamicNode.get("name")).getValue();
                String descriptor = ((ValueNode<String>) constDynamicNode.get("descriptor")).getValue();
                Handle handle = getHandle((MapNode) constDynamicNode.get("handle"));
                Object[] args = getArguments((ListNode) constDynamicNode.get("args"));
                arguments[i] = new ConstantDynamic(name, descriptor, handle, args);
            }
        }

        return arguments;
    }

    private Handle getHandle(MapNode handleNode) {
        int tag = ((ValueNode<Integer>) handleNode.get("tag")).getValue();
        String owner = ((ValueNode<String>) handleNode.get("owner")).getValue();
        String name = ((ValueNode<String>) handleNode.get("name")).getValue();
        String descriptor = ((ValueNode<String>) handleNode.get("descriptor")).getValue();
        boolean isInterface = ((ValueNode<Boolean>) handleNode.get("isInterface")).getValue();

        return new Handle(tag, owner, name, descriptor, isInterface);
    }
}
