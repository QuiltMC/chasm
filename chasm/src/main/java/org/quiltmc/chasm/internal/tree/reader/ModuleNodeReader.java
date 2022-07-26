package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.chasm.internal.util.NodeConstants;
import org.quiltmc.chasm.internal.util.NodeUtils;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public class ModuleNodeReader {
    private final MapNode moduleNode;

    public ModuleNodeReader(MapNode moduleNode) {
        this.moduleNode = moduleNode;
    }

    public void visitModule(ClassVisitor visitor) {
        String name = NodeUtils.getAsString(moduleNode, NodeConstants.NAME);
        int access = NodeUtils.getAsInt(moduleNode, NodeConstants.ACCESS);
        String version = NodeUtils.getAsString(moduleNode, NodeConstants.VERSION);

        ModuleVisitor moduleVisitor = visitor.visitModule(name, access, version);

        // visitMainClass
        visitMainClass(moduleVisitor);

        // visitPackage
        visitPackages(moduleVisitor);

        // visitRequire
        visitRequires(moduleVisitor);

        // visitExport
        visitExports(moduleVisitor);

        // visitOpen
        visitOpens(moduleVisitor);

        // visitUse
        visitUses(moduleVisitor);

        // visitProvide
        visitProvides(moduleVisitor);

        // visitEnd
        moduleVisitor.visitEnd();
    }

    private void visitMainClass(ModuleVisitor moduleVisitor) {
        String main = NodeUtils.getAsString(moduleNode, NodeConstants.MAIN);
        if (main != null) {
            moduleVisitor.visitMainClass(main);
        }
    }

    private void visitPackages(ModuleVisitor moduleVisitor) {
        // https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.26
        ListNode packagesListNode = NodeUtils.getAsList(moduleNode, NodeConstants.PACKAGES);
        if (packagesListNode == null) {
            return;
        }

        for (Node n : packagesListNode.getEntries()) {
            moduleVisitor.visitPackage(NodeUtils.asString(n));
        }
    }

    private void visitRequires(ModuleVisitor moduleVisitor) {
        ListNode moduleRequiresListNode = NodeUtils.getAsList(moduleNode, NodeConstants.REQUIRES);
        if (moduleRequiresListNode == null) {
            return;
        }
        for (Node n : moduleRequiresListNode.getEntries()) {
            MapNode requireNode = NodeUtils.asMap(n);
            String reqModule = NodeUtils.getAsString(requireNode, NodeConstants.MODULE);
            int reqAccess = NodeUtils.getAsInt(requireNode, NodeConstants.ACCESS);
            String reqVersion = NodeUtils.getAsString(requireNode, NodeConstants.VERSION);
            moduleVisitor.visitRequire(reqModule, reqAccess, reqVersion);
        }
    }

    private void visitExports(ModuleVisitor moduleVisitor) {
        ListNode moduleExportsListNode = NodeUtils.getAsList(moduleNode, NodeConstants.EXPORTS);
        if (moduleExportsListNode == null) {
            return;
        }
        for (Node n : moduleExportsListNode.getEntries()) {
            MapNode exportNode = NodeUtils.asMap(n);
            String expPackage = NodeUtils.getAsString(exportNode, NodeConstants.PACKAGE);
            int expAccess = NodeUtils.getAsInt(exportNode, NodeConstants.ACCESS);
            ListNode reqModules = NodeUtils.getAsList(exportNode, NodeConstants.MODULES);
            String[] modules = null;
            if (reqModules != null) {
                modules = new String[reqModules.getEntries().size()];
                for (int i = 0; i < reqModules.getEntries().size(); i++) {
                    modules[i] = NodeUtils.asString(reqModules.getEntries().get(i));
                }
            }
            moduleVisitor.visitExport(expPackage, expAccess, modules);
        }
    }

    private void visitOpens(ModuleVisitor moduleVisitor) {
        ListNode moduleOpensListNode = NodeUtils.getAsList(moduleNode, NodeConstants.OPENS);
        if (moduleOpensListNode == null) {
            return;
        }
        for (Node n : moduleOpensListNode.getEntries()) {
            MapNode openNode = NodeUtils.asMap(n);
            String openPackage = NodeUtils.getAsString(openNode, NodeConstants.PACKAGE);
            int openAccess = NodeUtils.getAsInt(openNode, NodeConstants.ACCESS);
            ListNode openModules = NodeUtils.getAsList(openNode, NodeConstants.MODULES);
            String[] modules = null;
            if (openModules != null) {
                modules = new String[openModules.getEntries().size()];
                for (int i = 0; i < openModules.getEntries().size(); i++) {
                    modules[i] = NodeUtils.asString(openModules.getEntries().get(i));
                }
            }

            moduleVisitor.visitOpen(openPackage, openAccess, modules);
        }
    }

    private void visitUses(ModuleVisitor moduleVisitor) {
        ListNode moduleUsesListNode = NodeUtils.getAsList(moduleNode, NodeConstants.USES);
        if (moduleUsesListNode == null) {
            return;
        }
        for (Node n : moduleUsesListNode.getEntries()) {
            moduleVisitor.visitUse(NodeUtils.asString(n));
        }
    }

    private void visitProvides(ModuleVisitor moduleVisitor) {
        ListNode moduleProvidesListNode = NodeUtils.getAsList(moduleNode, NodeConstants.PROVIDES);
        if (moduleProvidesListNode == null) {
            return;
        }
        for (Node n : moduleProvidesListNode.getEntries()) {
            MapNode providesNode = NodeUtils.asMap(n);
            String service = NodeUtils.getAsString(providesNode, NodeConstants.SERVICE);
            ListNode providers = NodeUtils.getAsList(providesNode, NodeConstants.PROVIDERS);
            String[] prov = new String[providers.getEntries().size()];
            for (int i = 0; i < providers.getEntries().size(); i++) {
                prov[i] = NodeUtils.asString(providers.getEntries().get(i));
            }
            moduleVisitor.visitProvide(service, prov);
        }
    }
}
