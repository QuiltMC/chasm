package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A set of helper functions to create a chassembly AST in Java code.
 */
public final class Ast {
    private Ast() {
    }

    /**
     * Creates a literal boolean node.
     *
     * @see BooleanNode
     */
    public static BooleanNode literal(boolean value) {
        return BooleanNode.from(value);
    }

    /**
     * Creates a literal float node.
     *
     * @see FloatNode
     */
    public static FloatNode literal(double value) {
        return new FloatNode(value);
    }

    /**
     * Creates a literal integer node from a character.
     *
     * @see #literal(long)
     * @see IntegerNode
     */
    public static IntegerNode literal(char value) {
        return literal((long) value);
    }

    /**
     * Creates a literal integer node.
     *
     * @see IntegerNode
     */
    public static IntegerNode literal(long value) {
        return new IntegerNode(value);
    }

    /**
     * Creates a literal string node.
     *
     * <p>If the argument may be null, use {@linkplain #nullableString(String)} instead.
     *
     * @see StringNode
     */
    public static StringNode literal(@NotNull String value) {
        return new StringNode(value);
    }

    /**
     * Creates a literal string node if the argument is not null, and a null node otherwise.
     */
    public static Node nullableString(@Nullable String value) {
        return value == null ? nullNode() : literal(value);
    }

    /**
     * Creates a null node.
     *
     * @see NullNode
     */
    public static NullNode nullNode() {
        return NullNode.INSTANCE;
    }

    /**
     * Creates an empty list node.
     *
     * @see #list()
     * @see ListNode
     */
    public static ListNode emptyList() {
        return new ListNode(new ArrayList<>(0));
    }

    /**
     * Starts building a list node.
     *
     * @see ListNode
     */
    public static ListNode.Builder list() {
        return new ListNode.Builder();
    }

    /**
     * Creates a list node made up of the given values.
     *
     * <p>Equivalent to {@code list().addAll(values).build()}.
     *
     * @see #list()
     * @see ListNode
     */
    public static ListNode list(@Nullable Object @NotNull ... values) {
        return list().addAll(values).build();
    }

    /**
     * Creates a list node made up of the given values.
     *
     * <p>Equivalent to {@code list().addAll(values).build()}.
     *
     * @see #list()
     * @see ListNode
     */
    public static ListNode list(@NotNull Iterable<?> values) {
        return list().addAll(values).build();
    }

    /**
     * Creates an empty map node.
     *
     * @see #map()
     * @see MapNode
     */
    public static MapNode emptyMap() {
        return new MapNode(new LinkedHashMap<>(0));
    }

    /**
     * Starts building a map node.
     *
     * @see MapNode
     */
    public static MapNode.Builder map() {
        return new MapNode.Builder();
    }

    /**
     * Creates a map node made up of the given entries.
     *
     * <p>Equivalent to {@code map().putAll(values).build()}.
     *
     * @see #map()
     * @see MapNode
     */
    public static MapNode map(Map<String, ?> map) {
        return map().putAll(map).build();
    }

    /**
     * Creates a local reference node.
     *
     * @see ReferenceNode
     */
    public static ReferenceNode ref(String referenceName) {
        return new ReferenceNode(referenceName, false);
    }

    /**
     * Creates a global reference node.
     *
     * @see ReferenceNode
     */
    public static ReferenceNode globalRef(String referenceName) {
        return new ReferenceNode(referenceName, true);
    }

    /**
     * Creates an index node for a variable by name and with a literal index.
     *
     * @see #index(Node, Node)
     * @see IndexNode
     */
    public static IndexNode index(String left, long index) {
        return index(ref(left), literal(index));
    }

    /**
     * Creates an index node for a variable by name.
     *
     * @see #index(Node, Node)
     * @see IndexNode
     */
    public static IndexNode index(String left, @NotNull Node index) {
        return index(ref(left), index);
    }

    /**
     * Creates an index node with a literal index.
     *
     * @see #index(Node, Node)
     * @see IndexNode
     */
    public static IndexNode index(@NotNull Node left, long index) {
        return index(left, literal(index));
    }

    /**
     * Creates an index node.
     *
     * @see IndexNode
     */
    public static IndexNode index(@NotNull Node left, @NotNull Node index) {
        return new IndexNode(left, index);
    }

    /**
     * Creates a member node for a variable by name.
     *
     * @see #member(Node, String)
     * @see MemberNode
     */
    public static MemberNode member(String owner, String member) {
        return member(ref(owner), member);
    }

    /**
     * Creates a member node.
     *
     * @see MemberNode
     */
    public static MemberNode member(@NotNull Node owner, String member) {
        return new MemberNode(owner, member);
    }

    /**
     * Creates a lambda node.
     *
     * @see LambdaNode
     */
    public static LambdaNode lambda(String argument, @NotNull Node body) {
        return new LambdaNode(argument, body);
    }

    /**
     * Creates a call node for a function by name.
     *
     * @see #call(Node, Node)
     * @see CallNode
     */
    public static CallNode call(String function, @NotNull Node arg) {
        return call(ref(function), arg);
    }

    /**
     * Creates a call node.
     *
     * @see CallNode
     */
    public static CallNode call(@NotNull Node function, @NotNull Node arg) {
        return new CallNode(function, arg);
    }

    /**
     * Creates a unary operator node.
     *
     * @see #unary(UnaryNode.Operator, Node)
     * @see UnaryNode
     */
    public static UnaryNode unary(String operator, @NotNull Node operand) {
        UnaryNode.Operator op = UnaryNode.Operator.getOperator(operator);
        if (op == null) {
            throw new IllegalArgumentException("Invalid unary operator " + operator);
        }
        return unary(op, operand);
    }

    /**
     * Creates a unary operator node.
     *
     * @see UnaryNode
     */
    public static UnaryNode unary(UnaryNode.Operator operator, @NotNull Node operand) {
        return new UnaryNode(operand, operator);
    }

    /**
     * Creates a binary operator node.
     *
     * @see #binary(Node, BinaryNode.Operator, Node)
     * @see BinaryNode
     */
    public static BinaryNode binary(@NotNull Node left, String operator, @NotNull Node right) {
        BinaryNode.Operator op = BinaryNode.Operator.getOperator(operator);
        if (op == null) {
            throw new IllegalArgumentException("Invalid binary operator " + operator);
        }
        return binary(left, op, right);
    }

    /**
     * Creates a binary operator node.
     *
     * @see BinaryNode
     */
    public static BinaryNode binary(@NotNull Node left, BinaryNode.Operator operator, @NotNull Node right) {
        return new BinaryNode(left, operator, right);
    }

    /**
     * Creates a ternary node.
     *
     * @see TernaryNode
     */
    public static TernaryNode ternary(@NotNull Node condition, @NotNull Node trueValue, @NotNull Node falseValue) {
        return new TernaryNode(condition, trueValue, falseValue);
    }

    @SuppressWarnings("unchecked")
    static Node objectToNode(@Nullable Object object) {
        if (object == null) {
            return nullNode();
        } else if (object instanceof Boolean) {
            return literal((Boolean) object);
        } else if (object instanceof Float || object instanceof Double) {
            return literal(((Number) object).doubleValue());
        } else if (object instanceof Number) {
            return literal(((Number) object).longValue());
        } else if (object instanceof Character) {
            return literal((Character) object);
        } else if (object instanceof String) {
            return literal((String) object);
        } else if (object instanceof Map
                && ((Map<?, ?>) object).keySet().stream().allMatch(it -> it instanceof String)) {
            return map((Map<String, ?>) object);
        } else if (object instanceof Iterable) {
            return list((Iterable<?>) object);
        } else if (object instanceof ListNode.Builder) {
            return ((ListNode.Builder) object).build();
        } else if (object instanceof MapNode.Builder) {
            return ((MapNode.Builder) object).build();
        } else if (object instanceof Node) {
            return (Node) object;
        } else {
            throw new IllegalArgumentException("Cannot coerce object to node: " + object);
        }
    }
}
