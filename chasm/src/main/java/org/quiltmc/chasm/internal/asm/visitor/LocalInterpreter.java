package org.quiltmc.chasm.internal.asm.visitor;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Interpreter;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.quiltmc.chasm.api.util.ClassInfoProvider;

import java.util.ArrayList;
import java.util.List;

public class LocalInterpreter extends Interpreter<LocalValue> {
    private final MethodNode method;
    private final SimpleVerifier simpleVerifier;

    protected LocalInterpreter(MethodNode method, ClassInfoProvider classInfoProvider, Type currentClass, @Nullable Type currentSuperClass, List<Type> currentClassInterfaces, boolean isInterface) {
        super(Opcodes.ASM9);
        this.method = method;
        this.simpleVerifier = new SimpleVerifier(Opcodes.ASM9, currentClass, currentSuperClass, currentClassInterfaces, isInterface) {
            @Override
            protected Class<?> getClass(Type type) {
                throw new UnsupportedOperationException("This method shouldn't have been called, as the methods that call it have been overridden");
            }

            @Override
            protected boolean isSubTypeOf(BasicValue value, BasicValue expected) {
                return isAssignableFrom(expected.getType(), value.getType());
            }

            @Override
            protected boolean isInterface(Type type) {
                if (currentClass.equals(type)) {
                    return isInterface;
                }
                return type.getSort() == Type.OBJECT && classInfoProvider.isInterface(type.getInternalName());
            }

            @Override
            protected Type getSuperClass(Type type) {
                if (currentClass.equals(type)) {
                    return currentSuperClass;
                }
                if (type.getSort() != Type.OBJECT) {
                    return null;
                }
                String superClass = classInfoProvider.getSuperClass(type.getInternalName());
                return superClass == null ? null : Type.getObjectType(superClass);
            }

            @Override
            protected boolean isAssignableFrom(Type type1, Type type2) {
                if (type1.getDescriptor().equals("Ljava/lang/Object;")) {
                    return true;
                }
                if (type2.equals(SimpleVerifier.NULL_TYPE)) {
                    return true;
                }
                if (type1.getSort() == Type.ARRAY) {
                    if (type2.getSort() != Type.ARRAY) {
                        return false;
                    }
                    if (type1.getDimensions() != type2.getDimensions()) {
                        return false;
                    }
                    return isAssignableFrom(type1.getElementType(), type2.getElementType());
                }
                if (type1.getSort() == Type.OBJECT && type2.getSort() == Type.OBJECT) {
                    return classInfoProvider.isAssignable(type1.getInternalName(), type2.getInternalName());
                }
                return type1.equals(type2);
            }
        };
    }

    @Override
    public LocalValue newValue(@Nullable Type type) {
        BasicValue basicValue = simpleVerifier.newValue(type);
        return basicValue == null ? null : new LocalValue(basicValue.getType());
    }

    @Override
    public LocalValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
        BasicValue basicValue = simpleVerifier.newOperation(insn);
        if (basicValue == null) {
            return null;
        } else if (insn.getOpcode() == Opcodes.JSR) {
            return new LocalValue(basicValue.getType(), method.instructions, insn);
        } else {
            return new LocalValue(basicValue.getType());
        }
    }

    @Override
    public LocalValue copyOperation(AbstractInsnNode insn, LocalValue value) throws AnalyzerException {
        BasicValue basicValue = simpleVerifier.copyOperation(insn, new BasicValue(value.getType()));
        if (basicValue == null) {
            return null;
        } else if (insn.getOpcode() >= Opcodes.ISTORE && insn.getOpcode() <= Opcodes.ASTORE) {
            return new LocalValue(basicValue.getType(), method.instructions, insn);
        } else {
            return new LocalValue(basicValue.getType());
        }
    }

    @Override
    public LocalValue unaryOperation(AbstractInsnNode insn, LocalValue value) throws AnalyzerException {
        BasicValue basicValue = simpleVerifier.unaryOperation(insn, new BasicValue(value.getType()));
        if (basicValue == null) {
            return null;
        } else if (insn.getOpcode() == Opcodes.IINC) {
            return new LocalValue(basicValue.getType(), value.getSourceStores());
        } else {
            return new LocalValue(basicValue.getType());
        }
    }

    @Override
    public LocalValue binaryOperation(AbstractInsnNode insn, LocalValue value1, LocalValue value2)
            throws AnalyzerException {
        BasicValue basicValue = simpleVerifier.binaryOperation(insn, new BasicValue(value1.getType()), new BasicValue(value2.getType()));
        return basicValue == null ? null : new LocalValue(basicValue.getType());
    }

    @Override
    public LocalValue ternaryOperation(AbstractInsnNode insn, LocalValue value1, LocalValue value2, LocalValue value3)
            throws AnalyzerException {
        BasicValue basicValue = simpleVerifier.ternaryOperation(insn, new BasicValue(value1.getType()), new BasicValue(value2.getType()), new BasicValue(value3.getType()));
        return basicValue == null ? null : new LocalValue(basicValue.getType());
    }

    @Override
    public LocalValue naryOperation(AbstractInsnNode insn, List<? extends LocalValue> values) throws AnalyzerException {
        List<BasicValue> basics = new ArrayList<>(values.size());
        for (LocalValue value : values) {
            basics.add(new BasicValue(value.getType()));
        }
        BasicValue basicValue = simpleVerifier.naryOperation(insn, basics);
        return basicValue == null ? null : new LocalValue(basicValue.getType());
    }

    @Override
    public void returnOperation(AbstractInsnNode insn, LocalValue value, LocalValue expected) throws AnalyzerException {
        if (value == null || expected == null) {
            return;
        }
        simpleVerifier.returnOperation(insn, new BasicValue(value.getType()), new BasicValue(expected.getType()));
    }

    @Override
    public LocalValue merge(LocalValue value1, LocalValue value2) {
        BasicValue basicValue = simpleVerifier.merge(new BasicValue(value1.getType()), new BasicValue(value2.getType()));
        return basicValue == null ? null : new LocalValue(basicValue.getType(), LocalValue.mergeSourceStores(value1.getSourceStores(), value2.getSourceStores()));
    }
}
