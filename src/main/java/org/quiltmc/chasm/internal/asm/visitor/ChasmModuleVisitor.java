package org.quiltmc.chasm.internal.asm.visitor;

import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.chasm.api.tree.LinkedHashMapNode;
import org.quiltmc.chasm.api.tree.LinkedListNode;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ChasmModuleVisitor extends ModuleVisitor {
    private final MapNode moduleNode;

    private final ListNode packages = new LinkedListNode();
    private final ListNode requires = new LinkedListNode();
    private final ListNode exports = new LinkedListNode();
    private final ListNode opens = new LinkedListNode();
    private final ListNode uses = new LinkedListNode();
    private final ListNode provides = new LinkedListNode();

    public ChasmModuleVisitor(int api, MapNode moduleNode) {
        super(api);
        this.moduleNode = moduleNode;

        moduleNode.put(NodeConstants.PACKAGES, packages);
        moduleNode.put(NodeConstants.REQUIRES, requires);
        moduleNode.put(NodeConstants.EXPORTS, exports);
        moduleNode.put(NodeConstants.OPENS, opens);
        moduleNode.put(NodeConstants.USES, uses);
        moduleNode.put(NodeConstants.PROVIDERS, provides);
    }

    @Override
    public void visitMainClass(String mainClass) {
        moduleNode.put(NodeConstants.MAIN, new ValueNode<>(mainClass));
    }

    @Override
    public void visitPackage(String packaze) {
        packages.add(new ValueNode<>(packaze));
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        MapNode requireNode = new LinkedHashMapNode();
        requireNode.put(NodeConstants.MODULE, new ValueNode<>(module));
        requireNode.put(NodeConstants.ACCESS, new ValueNode<>(access));
        requireNode.put(NodeConstants.VERSION, new ValueNode<>(version));
        requires.add(requireNode);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        MapNode exportNode = new LinkedHashMapNode();
        exportNode.put(NodeConstants.PACKAGE, new ValueNode<>(packaze));
        exportNode.put(NodeConstants.ACCESS, new ValueNode<>(access));
        if (modules != null) {
            ListNode modulesNode = new LinkedListNode();
            for (String m : modules) {
                modulesNode.add(new ValueNode<>(m));
            }
            exportNode.put(NodeConstants.MODULES, modulesNode);
        }
        exports.add(exportNode);
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        MapNode openNode = new LinkedHashMapNode();
        openNode.put(NodeConstants.PACKAGE, new ValueNode<>(packaze));
        openNode.put(NodeConstants.ACCESS, new ValueNode<>(access));
        if (modules != null) {
            ListNode modulesNode = new LinkedListNode();
            for (String m : modules) {
                modulesNode.add(new ValueNode<>(m));
            }
            openNode.put(NodeConstants.MODULES, modulesNode);
        }
        opens.add(openNode);
    }

    @Override
    public void visitUse(String service) {
        uses.add(new ValueNode<>(service));
    }

    @Override
    public void visitProvide(String service, String... providers) {
        MapNode provideNode = new LinkedHashMapNode();
        provideNode.put(NodeConstants.SERVICE, new ValueNode<>(service));
        ListNode providersNode = new LinkedListNode();
        for (String provider : providers) {
            providersNode.add(new ValueNode<>(provider));
        }
        provideNode.put(NodeConstants.PROVIDERS, providersNode);
        provides.add(provideNode);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
