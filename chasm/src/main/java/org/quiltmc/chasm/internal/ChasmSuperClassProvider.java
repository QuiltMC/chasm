package org.quiltmc.chasm.internal;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.api.util.SuperClassProvider;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmSuperClassProvider implements SuperClassProvider {
    private static final String OBJECT = "java/lang/Object";

    private final SuperClassProvider parent;
    private final Map<String, String> classNameToSuperClass = new HashMap<>();

    @SuppressWarnings("unchecked")
    public ChasmSuperClassProvider(SuperClassProvider parent, ListNode classes) {
        this.parent = parent;

        for (Node node : classes) {
            MapNode classNode = (MapNode) node;
            ValueNode<String> className = (ValueNode<String>) classNode.get(NodeConstants.NAME);
            ValueNode<String> superName = (ValueNode<String>) classNode.get(NodeConstants.SUPER);
            classNameToSuperClass.put(className.getValue(), superName == null ? OBJECT : superName.getValue());
        }
    }

    @Override
    public String getSuperClass(String className) {
        String superName = classNameToSuperClass.get(className);

        if (superName == null) {
            return parent.getSuperClass(className);
        }

        return superName;
    }
}
