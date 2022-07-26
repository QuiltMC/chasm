package org.quiltmc.chasm.internal.asm.visitor;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;

public class ChasmModuleVisitor extends ModuleVisitor {
    private final MapNode moduleNode;

    private final ListNode packages = new ListNode(new ArrayList<>());
    private final ListNode requires = new ListNode(new ArrayList<>());
    private final ListNode exports = new ListNode(new ArrayList<>());
    private final ListNode opens = new ListNode(new ArrayList<>());
    private final ListNode uses = new ListNode(new ArrayList<>());
    private final ListNode provides = new ListNode(new ArrayList<>());

    public ChasmModuleVisitor(int api, MapNode moduleNode) {
        super(api);
        this.moduleNode = moduleNode;

        moduleNode.getEntries().put(NodeConstants.PACKAGES, packages);
        moduleNode.getEntries().put(NodeConstants.REQUIRES, requires);
        moduleNode.getEntries().put(NodeConstants.EXPORTS, exports);
        moduleNode.getEntries().put(NodeConstants.OPENS, opens);
        moduleNode.getEntries().put(NodeConstants.USES, uses);
        moduleNode.getEntries().put(NodeConstants.PROVIDERS, provides);
    }

    @Override
    public void visitMainClass(String mainClass) {
        moduleNode.getEntries().put(NodeConstants.MAIN, new StringNode(mainClass));
    }

    @Override
    public void visitPackage(String packaze) {
        packages.getEntries().add(new StringNode(packaze));
    }

    @Override
    public void visitRequire(String module, int access, String version) {
        MapNode requireNode = new MapNode(new LinkedHashMap<>());
        requireNode.getEntries().put(NodeConstants.MODULE, new StringNode(module));
        requireNode.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        requireNode.getEntries().put(NodeConstants.VERSION, new StringNode(version));
        requires.getEntries().add(requireNode);
    }

    @Override
    public void visitExport(String packaze, int access, String... modules) {
        MapNode exportNode = new MapNode(new LinkedHashMap<>());
        exportNode.getEntries().put(NodeConstants.PACKAGE, new StringNode(packaze));
        exportNode.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        if (modules != null) {
            ListNode modulesNode = new ListNode(new ArrayList<>());
            for (String m : modules) {
                modulesNode.getEntries().add(new StringNode(m));
            }
            exportNode.getEntries().put(NodeConstants.MODULES, modulesNode);
        }
        exports.getEntries().add(exportNode);
    }

    @Override
    public void visitOpen(String packaze, int access, String... modules) {
        MapNode openNode = new MapNode(new LinkedHashMap<>());
        openNode.getEntries().put(NodeConstants.PACKAGE, new StringNode(packaze));
        openNode.getEntries().put(NodeConstants.ACCESS, new IntegerNode(access));
        if (modules != null) {
            ListNode modulesNode = new ListNode(new ArrayList<>());
            for (String m : modules) {
                modulesNode.getEntries().add(new StringNode(m));
            }
            openNode.getEntries().put(NodeConstants.MODULES, modulesNode);
        }
        opens.getEntries().add(openNode);
    }

    @Override
    public void visitUse(String service) {
        uses.getEntries().add(new StringNode(service));
    }

    @Override
    public void visitProvide(String service, String... providers) {
        MapNode provideNode = new MapNode(new LinkedHashMap<>());
        provideNode.getEntries().put(NodeConstants.SERVICE, new StringNode(service));
        ListNode providersNode = new ListNode(new ArrayList<>());
        for (String provider : providers) {
            providersNode.getEntries().add(new StringNode(provider));
        }
        provideNode.getEntries().put(NodeConstants.PROVIDERS, providersNode);
        provides.getEntries().add(provideNode);
    }

    @Override
    public void visitEnd() {
        // Nothing to do here
    }
}
