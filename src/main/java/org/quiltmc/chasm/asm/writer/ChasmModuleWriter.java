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
        String name = ((ValueNode<String>) moduleNode.get(NodeConstants.NAME)).getValue();
        int access = ((ValueNode<Integer>) moduleNode.get(NodeConstants.ACCESS)).getValue();
        String version = ((ValueNode<String>) moduleNode.get(NodeConstants.VERSION)).getValue();
        
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
            moduleVisitor.visitMainClass(((ValueNode<String>) moduleNode.get(NodeConstants.MAIN)).getValue());
        }
    }

    private void visitPackages(ModuleVisitor moduleVisitor) {
        // https://docs.oracle.com/javase/specs/jvms/se17/html/jvms-4.html#jvms-4.7.26
        ListNode packagesListNode = (ListNode) moduleNode.get(NodeConstants.PACKAGES);
        if (packagesListNode == null) {
            return;
        }
        for (Node n : packagesListNode) {
            moduleVisitor.visitPackage(((ValueNode<String>) n).getValue());
        }
    }

    private void visitRequires(ModuleVisitor moduleVisitor) {
        ListNode moduleRequiresListNode = (ListNode) moduleNode.get(NodeConstants.REQUIRES);
        if (moduleRequiresListNode == null) {
            return;
        }
        for (Node n : moduleRequiresListNode) {
            MapNode requireNode = (MapNode) n;
            String reqModule = ((ValueNode<String>) requireNode.get(NodeConstants.MODULE)).getValue();
            int reqAccess = ((ValueNode<Integer>) requireNode.get(NodeConstants.ACCESS)).getValue();
            
            ValueNode<String> versionNode = (ValueNode<String>) requireNode.get(NodeConstants.VERSION);
            String reqVersion = versionNode == null ? null : versionNode.getValue();
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
            String expPackage = ((ValueNode<String>) exportNode.get(NodeConstants.PACKAGE)).getValue();
            Integer expAcccess = ((ValueNode<Integer>) exportNode.get(NodeConstants.ACCESS)).getValue();
            ListNode reqModules = ((ListNode) exportNode.get(NodeConstants.MODULES));
            String[] modules = null;
            if (reqModules != null) {
                modules = new String[reqModules.size()];
                for (int i = 0; i < reqModules.size(); i++) {
                    modules[i] = ((ValueNode<String>) reqModules.get(i)).getValue();
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
            String openPackage = ((ValueNode<String>) openNode.get(NodeConstants.PACKAGE)).getValue();
            Integer openAccess = ((ValueNode<Integer>) openNode.get(NodeConstants.ACCESS)).getValue();
            
            ListNode openModules = ((ListNode) openNode.get(NodeConstants.MODULES));
            String[] modules = null;
            if (openModules != null) {
                modules = new String[openModules.size()];
                for (int i = 0; i < openModules.size(); i++) {
                    modules[i] = ((ValueNode<String>) openModules.get(i)).getValue();
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
            moduleVisitor.visitUse(((ValueNode<String>) n).getValue());
        }
    }

    private void visitProvides(ModuleVisitor moduleVisitor) {
        ListNode moduleProvidesListNode = (ListNode) moduleNode.get(NodeConstants.PROVIDES);
        if (moduleProvidesListNode == null) {
            return;
        }
        for (Node n : moduleProvidesListNode) {
            MapNode providesNode = (MapNode) n;
            String service = ((ValueNode<String>) providesNode.get(NodeConstants.SERVICE)).getValue();
            ListNode providers = (ListNode) providesNode.get(NodeConstants.PROVIDERS);
            String[] prov = new String[providers.size()];
            for (int i = 0; i < providers.size(); i++) {
                prov[i] = ((ValueNode<String>) providers.get(i)).getValue();
            }
            moduleVisitor.visitProvide(service, prov);
        }
    }
}
