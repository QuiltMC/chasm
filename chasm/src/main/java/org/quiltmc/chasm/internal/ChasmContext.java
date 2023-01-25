package org.quiltmc.chasm.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.quiltmc.chasm.api.util.ClassInfo;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class ChasmContext implements Context {
    private final Context parent;
    private final ListNode classes;

    public ChasmContext(Context parent, ListNode classes) {
        this.parent = parent;
        this.classes = classes;
    }

    @Override
    public ClassInfo getClassInfo(String className) {
        for (Node classNode : classes.getEntries()) {
            String name = NodeUtils.getAsString(classNode, NodeConstants.NAME);
            if (!name.equals(className)) {
                continue;
            }

            String superName = NodeUtils.getAsString(classNode, NodeConstants.SUPER);
            int access = NodeUtils.getAsInt(classNode, NodeConstants.ACCESS);
            ListNode interfaces = NodeUtils.getAsList(classNode, NodeConstants.INTERFACES);

            return new ClassInfo(
                    className,
                    superName == null && !className.equals(ClassInfo.OBJECT) ? ClassInfo.OBJECT : superName,
                    interfaces.getEntries().stream().map(NodeUtils::asString).toArray(String[]::new),
                    (access & Opcodes.ACC_INTERFACE) != 0
            );
        }

        return null;
    }

    @Override
    public byte @Nullable [] readFile(String path) {
        return parent.readFile(path);
    }
}
