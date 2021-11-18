package org.quiltmc.chasm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.transformer.NodePath;
import org.quiltmc.chasm.transformer.SliceTarget;
import org.quiltmc.chasm.transformer.Target;
import org.quiltmc.chasm.transformer.Transformation;
import org.quiltmc.chasm.tree.ListNode;
import org.quiltmc.chasm.tree.Node;

public class SliceManager {
    private final Map<NodePath, List<SliceTarget>> nodeToSlice = new HashMap<>();
    private final Node root;

    public SliceManager(Node root) {
        this.root = root;
    }

    public void addSlice(SliceTarget target) {
        this.nodeToSlice.computeIfAbsent(target.getPath(), p -> new ArrayList<>()).add(target);
    }

    public void replaceSlice(SliceTarget target, ListNode replacement) {
        if (target.getPath().resolve(root) instanceof ListNode owner) {
            // The change in "virtual" indices
            int sizeChange = (owner.size() - replacement.size()) * 2;

            // Move all slice indices affected by this
            List<SliceTarget> targets = nodeToSlice.get(target.getPath());
            for (SliceTarget toMove : targets) {
                if (toMove.getStartIndex() >= target.getEndIndex()) {
                    toMove.setStartIndex(toMove.getStartIndex() + sizeChange);
                }
                if (toMove.getEndIndex() >= target.getEndIndex()) {
                    toMove.setEndIndex(toMove.getEndIndex() + sizeChange );
                }
            }

            // Convert "virtual" indices into real ones
            int realStart = target.getStartIndex() / 2;
            int realLength = target.getEndIndex() / 2 - realStart;

            // Remove old entries
            for (int i = 0; i < realLength; i++) {
                owner.remove(realStart);
            }

            // Insert new entries
            for (Node entry : replacement) {
                owner.add(realStart, entry);
            }
        }
        else {
            throw new UnsupportedOperationException("Can't place slice in non-list");
        }
    }

    void addSlices(TransformationSorter transformations) {
        for (Transformation transformation : transformations.get()) {
            if (transformation.getTarget() instanceof SliceTarget sliceTarget) {
                this.addSlice(sliceTarget);
            }
    
            for (Target target : transformation.getSources().values()) {
                if (target instanceof SliceTarget sliceTarget) {
                    this.addSlice(sliceTarget);
                }
            }
        }
    }
}
