package org.quiltmc.chasm.asm;

import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.chasm.tree.*;

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

        moduleNode.put("packages", packages);
        moduleNode.put("requires", requires);
        moduleNode.put("exports", exports);
        moduleNode.put("opens", opens);
        moduleNode.put("uses", uses);
        moduleNode.put("provides", provides);
    }

    @Override
    public void visitMainClass(String mainClass) {
        moduleNode.put("main", new ValueNode<>(mainClass));
    }

    @Override
    public void visitPackage(String packaze) {
        packages.add(new ValueNode<>(packaze));
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        MapNode requireNode = new LinkedHashMapNode();
        requireNode.put("module", new ValueNode<>(module));
        requireNode.put("access", new ValueNode<>(access));
        requireNode.put("version", new ValueNode<>(version));
        requires.add(requireNode);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        MapNode exportNode = new LinkedHashMapNode();
        exportNode.put("package", new ValueNode<>(packaze));
        exportNode.put("access", new ValueNode<>(access));
        if (modules != null) {
            ListNode modulesNode = new LinkedListNode();
            for (String m : modules) {
                modulesNode.add(new ValueNode<>(m));
            }
            exportNode.put("modules", modulesNode);
        }
        exports.add(exportNode);
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        MapNode openNode = new LinkedHashMapNode();
        openNode.put("package", new ValueNode<>(packaze));
        openNode.put("access", new ValueNode<>(access));
        if (modules != null) {
            ListNode modulesNode = new LinkedListNode();
            for (String m : modules) {
                modulesNode.add(new ValueNode<>(m));
            }
            openNode.put("modules", modulesNode);
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
        provideNode.put("service", new ValueNode<>(service));
        ListNode providersNode = new LinkedListNode();
        for (String provider : providers) {
            providersNode.add(new ValueNode<>(provider));
        }
        provideNode.put("providers", providersNode);
        provides.add(provideNode);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
