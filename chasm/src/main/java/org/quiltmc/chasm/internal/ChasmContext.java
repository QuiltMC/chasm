package org.quiltmc.chasm.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;

public class ChasmContext implements Context {
    private static final String OBJECT = "java/lang/Object";

    private final Context parent;
    private final ListNode classes;

    public ChasmContext(Context parent, ListNode classes) {
        this.parent = parent;
        this.classes = classes;
    }

    private ClassInfo getClassInfo(String className) {
        for (Node node : classes.getEntries()) {
            MapNode classNode = (MapNode) node;
            StringNode nameNode = (StringNode) classNode.getEntries().get(NodeConstants.NAME);
            if (!nameNode.getValue().equals(className)) {
                continue;
            }

            StringNode superNode = (StringNode) classNode.getEntries().get(NodeConstants.SUPER);
            IntegerNode accessNode = (IntegerNode) classNode.getEntries().get(NodeConstants.ACCESS);
            ListNode interfacesList = (ListNode) classNode.getEntries().get(NodeConstants.INTERFACES);
            List<String> interfaces;
            if (interfacesList == null) {
                interfaces = Collections.emptyList();
            } else {
                interfaces = interfacesList.getEntries().stream().map(n -> ((StringNode) n).getValue())
                        .collect(Collectors.toList());
            }
            return new ClassInfo(
                    superNode == null ? OBJECT : superNode.getValue(),
                    (accessNode.getValue() & Opcodes.ACC_INTERFACE) != 0,
                    interfaces
            );
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

    @Override
    public byte @Nullable [] readFile(String path) {
        return parent.readFile(path);
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
