package org.quiltmc.chasm.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.api.util.ClassInfoProvider;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmClassInfoProvider implements ClassInfoProvider {
    private static final String OBJECT = "java/lang/Object";

    private final ClassInfoProvider parent;
    private final ListNode classes;

    public ChasmClassInfoProvider(ClassInfoProvider parent, ListNode classes) {
        this.parent = parent;
        this.classes = classes;
    }

    private ClassInfo getClassInfo(String className) {
        for (Node node : classes) {
            MapNode classNode = Node.asMap(node);
            ValueNode nameNode = Node.asValue(classNode.get(NodeConstants.NAME));
            if (!nameNode.getValueAsString().equals(className)) {
                continue;
            }

            ValueNode superNode = Node.asValue(classNode.get(NodeConstants.SUPER));
            ValueNode accessNode = Node.asValue(classNode.get(NodeConstants.ACCESS));
            ListNode interfacesList = Node.asList(classNode.get(NodeConstants.INTERFACES));
            List<String> interfaces = interfacesList == null ? Collections.emptyList()
                    : interfacesList.stream().map(n -> Node.asValue(n).getValueAsString()).collect(Collectors.toList());
            return new ClassInfo(
                            superNode == null ? OBJECT : superNode.getValueAsString(),
                            (accessNode.getValueAsInt() & Opcodes.ACC_INTERFACE) != 0,
                            interfaces);
        }

        return null;
    }

    @Override
    public String getSuperClass(String className) {
        ClassInfo classInfo = getClassInfo(className);

        if (classInfo == null) {
            return parent.getSuperClass(className);
        }

        return classInfo.superClass;
    }

    @Override
    public boolean isInterface(String className) {
        ClassInfo classInfo = getClassInfo(className);

        if (classInfo == null) {
            return parent.isInterface(className);
        }

        return classInfo.isInterface;
    }

    @Override
    public boolean isAssignable(String leftClass, String rightClass) {
        // TODO: guard against infinite recursion

        if (rightClass.equals(leftClass)) {
            return true;
        }
        Set<String> interfacesToCheck = new HashSet<>(0);
        String tmpRightClass = rightClass;
        while (true) {
            ClassInfo rightInfo = getClassInfo(tmpRightClass);
            if (rightInfo == null) {
                return parent.isAssignable(leftClass, rightClass);
            }
            tmpRightClass = rightInfo.superClass;
            if (tmpRightClass == null) {
                break;
            }

            if (tmpRightClass.equals(leftClass)) {
                return true;
            }

            interfacesToCheck.addAll(rightInfo.interfaces);
        }

        for (String itf : interfacesToCheck) {
            if (isAssignable(leftClass, itf)) {
                return true;
            }
        }

        return false;
    }

    private static class ClassInfo {
        final String superClass;
        final boolean isInterface;
        final List<String> interfaces;

        private ClassInfo(String superClass, boolean isInterface, List<String> interfaces) {
            this.superClass = superClass;
            this.isInterface = isInterface;
            this.interfaces = interfaces;
        }
    }
}
