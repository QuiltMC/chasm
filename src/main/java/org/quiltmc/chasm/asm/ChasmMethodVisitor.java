package org.quiltmc.chasm.asm;

import org.objectweb.asm.MethodVisitor;
import org.quiltmc.chasm.tree.ChasmMap;

public class ChasmMethodVisitor extends MethodVisitor {
    private final ChasmMap methodNode;

    public ChasmMethodVisitor(int api, ChasmMap methodNode) {
        super(api);

        this.methodNode = methodNode;
    }
}
