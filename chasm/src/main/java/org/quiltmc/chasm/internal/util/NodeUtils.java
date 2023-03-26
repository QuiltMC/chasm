package org.quiltmc.chasm.internal.util;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.BooleanNode;
import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.internal.Assert;

public class NodeUtils {
    private NodeUtils() {
    }

    public static Node get(Node node, String key) {
        return asMap(node).get(key);
    }

    public static boolean has(Node node, String key) {
        return asMap(node).getEntries().containsKey(key);
    }

    public static MapNode getAsMap(Node node, String key) {
        return asMap(get(node, key));
    }

    public static ListNode getAsList(Node node, String key) {
        return asList(get(node, key));
    }

    public static String getAsString(Node node, String key) {
        return asString(get(node, key));
    }

    public static Long getAsLong(Node node, String key) {
        return asLong(get(node, key));
    }

    public static Boolean getAsBoolean(Node node, String key) {
        return asBoolean(get(node, key));
    }

    public static int getAsInt(Node node, String key) {
        return getAsLong(node, key).intValue();
    }

    public static MapNode asMap(Node node) {
        if (node == null || node instanceof NullNode) {
            return null;
        }
        if (node instanceof MapNode) {
            return (MapNode) node;
        }
        throw createWrongTypeException(node, MapNode.class);
    }

    public static ListNode asList(Node node) {
        if (node == null || node instanceof NullNode) {
            return null;
        }
        if (node instanceof ListNode) {
            return (ListNode) node;
        }
        throw createWrongTypeException(node, ListNode.class);
    }

    public static String asString(Node node) {
        if (node == null || node instanceof NullNode) {
            return null;
        }
        if (node instanceof StringNode) {
            return ((StringNode) node).getValue();
        }
        throw createWrongTypeException(node, StringNode.class);
    }

    public static Long asLong(Node node) {
        if (node == null || node instanceof NullNode) {
            return null;
        }
        if (node instanceof IntegerNode) {
            return ((IntegerNode) node).getValue();
        }
        throw createWrongTypeException(node, IntegerNode.class);
    }

    public static Boolean asBoolean(Node node) {
        if (node == null || node instanceof NullNode) {
            return null;
        }
        if (node instanceof BooleanNode) {
            return ((BooleanNode) node).getValue();
        }
        throw createWrongTypeException(node, IntegerNode.class);
    }

    public static int asInt(Node node) {
        return asLong(node).intValue();
    }

    public static IllegalStateException createWrongTypeException(Node node, Class<?> clazz) {
        return new IllegalStateException("Expected " + clazz + " but found " + node);
    }

    public static Node getValueNode(Object value) {
        MapNode node = Ast.emptyMap();
        if (value instanceof Byte) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.BYTE));
            node.put(NodeConstants.VALUE, Ast.literal((long) value));
        } else if (value instanceof Boolean) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.BOOLEAN));
            node.put(NodeConstants.VALUE, Ast.literal((Boolean) value));
        } else if (value instanceof Character) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.CHARACTER));
            node.put(NodeConstants.VALUE, Ast.literal((Character) value));
        } else if (value instanceof Short) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.SHORT));
            node.put(NodeConstants.VALUE, Ast.literal((Short) value));
        } else if (value instanceof Integer) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.INTEGER));
            node.put(NodeConstants.VALUE, Ast.literal((Integer) value));
        } else if (value instanceof Long) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.LONG));
            node.put(NodeConstants.VALUE, Ast.literal((Long) value));
        } else if (value instanceof Float) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.FLOAT));
            node.put(NodeConstants.VALUE, Ast.literal((Float) value));
        } else if (value instanceof Double) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.DOUBLE));
            node.put(NodeConstants.VALUE, Ast.literal((Double) value));
        } else if (value instanceof String) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.STRING));
            node.put(NodeConstants.VALUE, Ast.literal((String) value));
        } else if (value instanceof Type) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.TYPE));
            node.put(NodeConstants.VALUE, Ast.literal(((Type) value).getDescriptor()));
        } else if (value instanceof Handle) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.HANDLE));
            node.put(NodeConstants.VALUE, getHandleNode((Handle) value));
        } else if (value instanceof ConstantDynamic) {
            node.put(NodeConstants.TYPE, Ast.literal(NodeConstants.CONSTANT_DYNAMIC));
            node.put(NodeConstants.VALUE, getConstantDynamicNode((ConstantDynamic) value));
        } else {
            throw new RuntimeException("Invalid annotation value: " + value);
        }

        return node;
    }

    public static Object fromValueNode(Node node) {
        String type = getAsString(node, NodeConstants.TYPE);
        Node valueNode = asMap(node).get(NodeConstants.VALUE);
        Assert.check(valueNode != null);

        switch (type) {
            case NodeConstants.BYTE:
                return ((IntegerNode) valueNode).getValue().byteValue();
            case NodeConstants.BOOLEAN:
                return ((BooleanNode) valueNode).getValue();
            case NodeConstants.CHARACTER:
                return (char) ((IntegerNode) valueNode).getValue().shortValue();
            case NodeConstants.SHORT:
                return ((IntegerNode) valueNode).getValue().shortValue();
            case NodeConstants.INTEGER:
                return ((IntegerNode) valueNode).getValue().intValue();
            case NodeConstants.LONG:
                return ((IntegerNode) valueNode).getValue();
            case NodeConstants.FLOAT:
                return ((FloatNode) valueNode).getValue().floatValue();
            case NodeConstants.DOUBLE:
                return ((FloatNode) valueNode).getValue();
            case NodeConstants.STRING:
                return ((StringNode) valueNode).getValue();
            case NodeConstants.TYPE:
                return Type.getType(((StringNode) valueNode).getValue());
            case NodeConstants.HANDLE:
                return asHandle(valueNode);
            case NodeConstants.CONSTANT_DYNAMIC:
                return asConstantDynamic(valueNode);
            default:
                throw new RuntimeException("Invalid value type: " + type);
        }
    }

    public static ListNode getValueListNode(Object[] values) {
        ListNode argumentsNode = Ast.emptyList();

        for (Object value : values) {
            argumentsNode.add(getValueNode(value));
        }

        return argumentsNode;
    }

    public static MapNode getHandleNode(Handle handle) {
        return Ast.map()
                .put(NodeConstants.TAG, handle.getTag())
                .put(NodeConstants.OWNER, handle.getOwner())
                .put(NodeConstants.NAME, handle.getName())
                .put(NodeConstants.DESCRIPTOR, handle.getDesc())
                .put(NodeConstants.IS_INTERFACE, handle.isInterface())
                .build();
    }

    public static Handle asHandle(Node node) {
        int tag = getAsInt(node, NodeConstants.TAG);
        String owner = getAsString(node, NodeConstants.OWNER);
        String name = getAsString(node, NodeConstants.NAME);
        String descriptor = getAsString(node, NodeConstants.DESCRIPTOR);
        Boolean isInterface = getAsBoolean(node, NodeConstants.IS_INTERFACE);

        if (isInterface == null) {
            isInterface = false;
        }

        return new Handle(tag, owner, name, descriptor, isInterface);
    }

    public static MapNode getConstantDynamicNode(ConstantDynamic constantDynamic) {
        Object[] arguments = new Object[constantDynamic.getBootstrapMethodArgumentCount()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = constantDynamic.getBootstrapMethodArgument(i);
        }

        return Ast.map()
                .put(NodeConstants.NAME, constantDynamic.getName())
                .put(NodeConstants.DESCRIPTOR, constantDynamic.getDescriptor())
                .put(NodeConstants.HANDLE, getHandleNode(constantDynamic.getBootstrapMethod()))
                .put(NodeConstants.ARGUMENTS, getValueListNode(arguments))
                .build();
    }

    public static ConstantDynamic asConstantDynamic(Node node) {
        String name = getAsString(node, NodeConstants.NAME);
        String descriptor = getAsString(node, NodeConstants.DESCRIPTOR);
        Handle handle = asHandle(getAsMap(node, NodeConstants.HANDLE));

        ListNode arguments = getAsList(node, NodeConstants.ARGUMENTS);
        Object[] args = new Object[arguments.size()];
        int i = 0;
        for (Node entry : arguments.getEntries()) {
            args[i++] = fromValueNode(entry);
        }

        return new ConstantDynamic(name, descriptor, handle, args);
    }
}
