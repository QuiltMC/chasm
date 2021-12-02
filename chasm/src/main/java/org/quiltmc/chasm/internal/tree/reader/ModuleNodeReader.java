package org.quiltmc.chasm.internal.tree.reader;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.tree.ValueNode;
import org.quiltmc.chasm.internal.util.NodeConstants;

public class ModuleNodeReader {
    private final MapNode moduleNode;

    public ModuleNodeReader(MapNode moduleNode) {
        this.moduleNode = moduleNode;
    }

    public void visitModule(ClassVisitor visitor) {
        String name = Node.asValue(moduleNode.get(NodeConstants.NAME)).getValueAsString();
        int access = Node.asValue(moduleNode.get(NodeConstants.ACCESS)).getValueAsInt();
        ValueNode versionNode = Node.asValue(moduleNode.get(NodeConstants.VERSION));
        String version = versionNode == null ? null : versionNode.getValueAsString();

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
        if (moduleNode.containsKey(NodeConstants.MAIN)) {
            moduleVisitor.visitMainClass(Node.asValue(moduleNode.get(NodeConstants.MAIN)).getValueAsString());
        }
    }

    private void visitPackages(ModuleVisitor moduleVisitor) {
        // https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.26
        ListNode packagesListNode = Node.asList(moduleNode.get(NodeConstants.PACKAGES));
        if (packagesListNode == null) {
            return;
        }
        for (Node n : packagesListNode) {
            moduleVisitor.visitPackage(Node.asValue(n).getValueAsString());
        }
    }

    private void visitRequires(ModuleVisitor moduleVisitor) {
        ListNode moduleRequiresListNode = Node.asList(moduleNode.get(NodeConstants.REQUIRES));
        if (moduleRequiresListNode == null) {
            return;
        }
        for (Node n : moduleRequiresListNode) {
            MapNode requireNode = Node.asMap(n);
            String reqModule = Node.asValue(requireNode.get(NodeConstants.MODULE)).getValueAsString();
            int reqAccess = Node.asValue(requireNode.get(NodeConstants.ACCESS)).getValueAsInt();

            ValueNode versionNode = Node.asValue(requireNode.get(NodeConstants.VERSION));
            String reqVersion = versionNode == null ? null : versionNode.getValueAsString();
            moduleVisitor.visitRequire(reqModule, reqAccess, reqVersion);
        }
    }

    private void visitExports(ModuleVisitor moduleVisitor) {
        ListNode moduleExportsListNode = Node.asList(moduleNode.get(NodeConstants.EXPORTS));
        if (moduleExportsListNode == null) {
            return;
        }
        for (Node n : moduleExportsListNode) {
            MapNode exportNode = Node.asMap(n);
            String expPackage = Node.asValue(exportNode.get(NodeConstants.PACKAGE)).getValueAsString();
            Integer expAcccess = Node.asValue(exportNode.get(NodeConstants.ACCESS)).getValueAsInt();
            ListNode reqModules = (Node.asList(exportNode.get(NodeConstants.MODULES)));
            String[] modules = null;
            if (reqModules != null) {
                modules = new String[reqModules.size()];
                for (int i = 0; i < reqModules.size(); i++) {
                    modules[i] = Node.asValue(reqModules.get(i)).getValueAsString();
                }
            }
            moduleVisitor.visitExport(expPackage, expAcccess, modules);
        }
    }

    private void visitOpens(ModuleVisitor moduleVisitor) {
        ListNode moduleOpensListNode = Node.asList(moduleNode.get(NodeConstants.OPENS));
        if (moduleOpensListNode == null) {
            return;
        }
        for (Node n : moduleOpensListNode) {
            MapNode openNode = Node.asMap(n);
            String openPackage = Node.asValue(openNode.get(NodeConstants.PACKAGE)).getValueAsString();
            Integer openAccess = Node.asValue(openNode.get(NodeConstants.ACCESS)).getValueAsInt();

            ListNode openModules = (Node.asList(openNode.get(NodeConstants.MODULES)));
            String[] modules = null;
            if (openModules != null) {
                modules = new String[openModules.size()];
                for (int i = 0; i < openModules.size(); i++) {
                    modules[i] = Node.asValue(openModules.get(i)).getValueAsString();
                }
            }

            moduleVisitor.visitOpen(openPackage, openAccess, modules);
        }
    }

    private void visitUses(ModuleVisitor moduleVisitor) {
        ListNode moduleUsesListNode = Node.asList(moduleNode.get(NodeConstants.USES));
        if (moduleUsesListNode == null) {
            return;
        }
        for (Node n : moduleUsesListNode) {
            moduleVisitor.visitUse(Node.asValue(n).getValueAsString());
        }
    }

    private void visitProvides(ModuleVisitor moduleVisitor) {
        ListNode moduleProvidesListNode = Node.asList(moduleNode.get(NodeConstants.PROVIDES));
        if (moduleProvidesListNode == null) {
            return;
        }
        for (Node n : moduleProvidesListNode) {
            MapNode providesNode = Node.asMap(n);
            String service = Node.asValue(providesNode.get(NodeConstants.SERVICE)).getValueAsString();
            ListNode providers = Node.asList(providesNode.get(NodeConstants.PROVIDERS));
            String[] prov = new String[providers.size()];
            for (int i = 0; i < providers.size(); i++) {
                prov[i] = Node.asValue(providers.get(i)).getValueAsString();
            }
            moduleVisitor.visitProvide(service, prov);
        }
    }
}
