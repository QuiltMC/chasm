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
        String name = ((ValueNode) moduleNode.get(NodeConstants.NAME)).getValueAsString();
        int access = ((ValueNode) moduleNode.get(NodeConstants.ACCESS)).getValueAsInt();
        ValueNode versionNode = (ValueNode) moduleNode.get(NodeConstants.VERSION);
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
            moduleVisitor.visitMainClass(((ValueNode) moduleNode.get(NodeConstants.MAIN)).getValueAsString());
        }
    }

    private void visitPackages(ModuleVisitor moduleVisitor) {
        // https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.26
        ListNode packagesListNode = (ListNode) moduleNode.get(NodeConstants.PACKAGES);
        if (packagesListNode == null) {
            return;
        }
        for (Node n : packagesListNode) {
            moduleVisitor.visitPackage(((ValueNode) n).getValueAsString());
        }
    }

    private void visitRequires(ModuleVisitor moduleVisitor) {
        ListNode moduleRequiresListNode = (ListNode) moduleNode.get(NodeConstants.REQUIRES);
        if (moduleRequiresListNode == null) {
            return;
        }
        for (Node n : moduleRequiresListNode) {
            MapNode requireNode = (MapNode) n;
            String reqModule = ((ValueNode) requireNode.get(NodeConstants.MODULE)).getValueAsString();
            int reqAccess = ((ValueNode) requireNode.get(NodeConstants.ACCESS)).getValueAsInt();

            ValueNode versionNode = (ValueNode) requireNode.get(NodeConstants.VERSION);
            String reqVersion = versionNode == null ? null : versionNode.getValueAsString();
            moduleVisitor.visitRequire(reqModule, reqAccess, reqVersion);
        }
    }

    private void visitExports(ModuleVisitor moduleVisitor) {
        ListNode moduleExportsListNode = (ListNode) moduleNode.get(NodeConstants.EXPORTS);
        if (moduleExportsListNode == null) {
            return;
        }
        for (Node n : moduleExportsListNode) {
            MapNode exportNode = (MapNode) n;
            String expPackage = ((ValueNode) exportNode.get(NodeConstants.PACKAGE)).getValueAsString();
            Integer expAcccess = ((ValueNode) exportNode.get(NodeConstants.ACCESS)).getValueAsInt();
            ListNode reqModules = ((ListNode) exportNode.get(NodeConstants.MODULES));
            String[] modules = null;
            if (reqModules != null) {
                modules = new String[reqModules.size()];
                for (int i = 0; i < reqModules.size(); i++) {
                    modules[i] = ((ValueNode) reqModules.get(i)).getValueAsString();
                }
            }
            moduleVisitor.visitExport(expPackage, expAcccess, modules);
        }
    }

    private void visitOpens(ModuleVisitor moduleVisitor) {
        ListNode moduleOpensListNode = (ListNode) moduleNode.get(NodeConstants.OPENS);
        if (moduleOpensListNode == null) {
            return;
        }
        for (Node n : moduleOpensListNode) {
            MapNode openNode = (MapNode) n;
            String openPackage = ((ValueNode) openNode.get(NodeConstants.PACKAGE)).getValueAsString();
            Integer openAccess = ((ValueNode) openNode.get(NodeConstants.ACCESS)).getValueAsInt();

            ListNode openModules = ((ListNode) openNode.get(NodeConstants.MODULES));
            String[] modules = null;
            if (openModules != null) {
                modules = new String[openModules.size()];
                for (int i = 0; i < openModules.size(); i++) {
                    modules[i] = ((ValueNode) openModules.get(i)).getValueAsString();
                }
            }

            moduleVisitor.visitOpen(openPackage, openAccess, modules);
        }
    }

    private void visitUses(ModuleVisitor moduleVisitor) {
        ListNode moduleUsesListNode = (ListNode) moduleNode.get(NodeConstants.USES);
        if (moduleUsesListNode == null) {
            return;
        }
        for (Node n : moduleUsesListNode) {
            moduleVisitor.visitUse(((ValueNode) n).getValueAsString());
        }
    }

    private void visitProvides(ModuleVisitor moduleVisitor) {
        ListNode moduleProvidesListNode = (ListNode) moduleNode.get(NodeConstants.PROVIDES);
        if (moduleProvidesListNode == null) {
            return;
        }
        for (Node n : moduleProvidesListNode) {
            MapNode providesNode = (MapNode) n;
            String service = ((ValueNode) providesNode.get(NodeConstants.SERVICE)).getValueAsString();
            ListNode providers = (ListNode) providesNode.get(NodeConstants.PROVIDERS);
            String[] prov = new String[providers.size()];
            for (int i = 0; i < providers.size(); i++) {
                prov[i] = ((ValueNode) providers.get(i)).getValueAsString();
            }
            moduleVisitor.visitProvide(service, prov);
        }
    }
}
