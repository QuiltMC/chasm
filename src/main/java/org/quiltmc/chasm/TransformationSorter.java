package org.quiltmc.chasm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.quiltmc.chasm.transformer.Transformation;

public class TransformationSorter {
    private final List<Transformation> transformations = new LinkedList<>();

    private static Order getOrder(Transformation first, Transformation second) {
        // Case 6a/7a/8a
        if (first.getTarget().contains(second.getTarget())) {
            return Order.MUST_RUN_AFTER;
        }
        if (second.getTarget().contains(first.getTarget())) {
            return Order.MUST_RUN_BEFORE;
        }

        // Case 6b/7b/8b
        if (first.getSources().values().stream().anyMatch(second.getTarget()::contains)) {
            return Order.MUST_RUN_BEFORE;
        }
        if (second.getSources().values().stream().anyMatch(first.getTarget()::contains)) {
            return Order.MUST_RUN_AFTER;
        }

        //Case 2a
        if (first.getTarget().overlaps(second.getTarget())) {
            return Order.MUST_RUN_AFTER;
        }
        if (second.getTarget().overlaps(first.getTarget())) {
            return Order.MUST_RUN_BEFORE;
        }

        // Case 2b/2c
        if (first.getSources().values().stream().anyMatch(second.getTarget()::overlaps)) {
            return Order.MUST_RUN_BEFORE;
        }
        if (second.getSources().values().stream().anyMatch(first.getTarget()::overlaps)) {
            return Order.MUST_RUN_AFTER;
        }

        //Case 6c/7c/8c
        if (first.getSources().values().stream().anyMatch(s -> s.contains(second.getTarget()))) {
            return Order.SHOULD_RUN_AFTER;
        }
        if (second.getSources().values().stream().anyMatch(s -> s.contains(first.getTarget()))) {
            return Order.SHOULD_RUN_BEFORE;
        }

        return Order.DONT_CARE;
    }

    public void add(Transformation transformation) {
        insertSorted(transformation);
    }

    public void addAll(Collection<? extends Transformation> c) {
        c.forEach(this::add);
    }

    public List<Transformation> get() {
        return transformations;
    }

    private void insertSorted(Transformation transformation) {
        int mustBefore = transformations.size();
        int mustAfter = -1;
        int shouldBefore = transformations.size();
        int shouldAfter = -1;
        for (int i = 0; i < transformations.size(); i++) {
            Transformation other = transformations.get(i);
            Order order = getOrder(transformation, other);
            Order reverseOrder = getOrder(other, transformation);
            if (order == Order.MUST_RUN_AFTER && reverseOrder == Order.MUST_RUN_AFTER
                    || order == Order.MUST_RUN_BEFORE && reverseOrder == Order.MUST_RUN_BEFORE) {
                throw new RuntimeException("Can't apply transformations without conflict.");
            }
            if (order == Order.SHOULD_RUN_AFTER && reverseOrder == Order.SHOULD_RUN_AFTER
                    || order == Order.SHOULD_RUN_BEFORE && reverseOrder == Order.SHOULD_RUN_BEFORE) {
                continue;
            }
            switch (order) {
                case MUST_RUN_AFTER:
                    mustAfter = Math.max(mustAfter, i);
                    break;
                case MUST_RUN_BEFORE:
                    mustBefore = Math.min(mustBefore, i);
                    break;
                case SHOULD_RUN_AFTER:
                    shouldAfter = Math.max(shouldAfter, i);
                    break;
                case SHOULD_RUN_BEFORE:
                    shouldBefore = Math.max(shouldBefore, i);
                    break;
                default:
                    break;
            }
        }

        if (!(mustAfter < mustBefore)) {
            throw new RuntimeException("Can't apply transformations without conflict.");
        }
        if (mustAfter < shouldAfter && shouldAfter < mustBefore) {
            mustAfter = shouldAfter;
        }
        if (mustAfter < shouldBefore && shouldBefore < mustBefore) {
            mustBefore = shouldBefore;
        }
        transformations.add(mustBefore, transformation);
    }

    private enum Order {
        MUST_RUN_BEFORE,
        SHOULD_RUN_BEFORE,
        DONT_CARE,
        SHOULD_RUN_AFTER,
        MUST_RUN_AFTER
    }
}
