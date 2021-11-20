package org.quiltmc.chasm.asm.writer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.quiltmc.chasm.NodeConstants;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.MapNode;
import org.quiltmc.chasm.tree.Node;
import org.quiltmc.chasm.tree.ValueNode;

@SuppressWarnings("unchecked")
public class ChasmModuleWriter {
    private final MapNode moduleNode;

    public ChasmModuleWriter(MapNode moduleNode) {
        this.moduleNode = moduleNode;
    }

    public void visitModule(ClassVisitor visitor) {
        String name = moduleNode.get(NodeConstants.NAME).getAsString();
        int access = moduleNode.get(NodeConstants.ACCESS).getAsInt();
        String version = moduleNode.get(NodeConstants.VERSION).getAsString();

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
            moduleVisitor.visitMainClass(moduleNode.get(NodeConstants.MAIN).getAsString());
        }
    }

    private void visitPackages(ModuleVisitor moduleVisitor) {
        for (Node n : (ListNode) moduleNode.get(NodeConstants.PACKAGES)) {
            moduleVisitor.visitPackage(n.getAsString());
        }
    }

    private void visitRequires(ModuleVisitor moduleVisitor) {
        for (Node n : (ListNode) moduleNode.get(NodeConstants.REQUIRES)) {
            MapNode requireNode = (MapNode) n;
            String reqModule = requireNode.get(NodeConstants.MODULE).getAsString();
            Integer reqAccess = requireNode.get(NodeConstants.ACCESS).getAsInt();
            String reqVersion = requireNode.get(NodeConstants.VERSION).getAsString();
            moduleVisitor.visitRequire(reqModule, reqAccess, reqVersion);
        }
    }

    private void visitExports(ModuleVisitor moduleVisitor) {
        for (Node n : (ListNode) moduleNode.get(NodeConstants.EXPORTS)) {
            MapNode exportNode = (MapNode) n;
            String expPackage = exportNode.get(NodeConstants.PACKAGE).getAsString();
            Integer expAcccess = exportNode.get(NodeConstants.ACCESS).getAsInt();
            ListNode reqModules = ((ListNode) exportNode.get(NodeConstants.MODULES));
            String[] modules = null;
            if (reqModules != null) {
                modules = new String[reqModules.size()];
                for (int i = 0; i < reqModules.size(); i++) {
                    modules[i] = reqModules.get(i).getAsString();
                }
            }
            moduleVisitor.visitExport(expPackage, expAcccess, modules);
        }
    }

    private void visitOpens(ModuleVisitor moduleVisitor) {
        for (Node n : (ListNode) moduleNode.get(NodeConstants.OPENS)) {
            MapNode openNode = (MapNode) n;
            String openPackage = openNode.get(NodeConstants.PACKAGE).getAsString();
            Integer openAcccess = openNode.get(NodeConstants.ACCESS).getAsInt();
            ListNode openModules = ((ListNode) openNode.get(NodeConstants.MODULES));
            String[] modules = null;
            if (openModules != null) {
                modules = new String[openModules.size()];
                for (int i = 0; i < openModules.size(); i++) {
                    modules[i] = openModules.get(i).getAsString();
                }
            }
            moduleVisitor.visitOpen(openPackage, openAcccess, modules);
        }
    }

    private void visitUses(ModuleVisitor moduleVisitor) {
        for (Node n : (ListNode) moduleNode.get(NodeConstants.USES)) {
            moduleVisitor.visitUse(n.getAsString());
        }
    }

    private void visitProvides(ModuleVisitor moduleVisitor) {
        for (Node n : (ListNode) moduleNode.get(NodeConstants.PROVIDES)) {
            MapNode providesNode = (MapNode) n;
            String service = providesNode.get(NodeConstants.SERVICE).getAsString();
            ListNode providers = (ListNode) providesNode.get(NodeConstants.PROVIDERS);
            String[] prov = new String[providers.size()];
            for (int i = 0; i < providers.size(); i++) {
                prov[i] = providers.get(i).getAsString();
            }
            moduleVisitor.visitProvide(service, prov);
        }
    }
}
