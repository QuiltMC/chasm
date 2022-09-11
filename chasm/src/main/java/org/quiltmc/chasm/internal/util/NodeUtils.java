package org.quiltmc.chasm.internal.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.quiltmc.chasm.lang.api.ast.BooleanNode;
import org.quiltmc.chasm.lang.api.ast.FloatNode;
import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;

public class NodeUtils {
    private NodeUtils() {
    }

    public static Node get(Node node, String key) {
        return asMap(node).getEntries().get(key);
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
        if (node == null) {
            return null;
        }
        if (node instanceof MapNode) {
            return (MapNode) node;
        }
        throw createWrongTypeException(node, MapNode.class);
    }

    public static ListNode asList(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ListNode) {
            return (ListNode) node;
        }
        throw createWrongTypeException(node, ListNode.class);
    }

    public static String asString(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof StringNode) {
            return ((StringNode) node).getValue();
        }
        throw createWrongTypeException(node, StringNode.class);
    }

    public static Long asLong(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof IntegerNode) {
            return ((IntegerNode) node).getValue();
        }
        throw createWrongTypeException(node, IntegerNode.class);
    }

    public static Boolean asBoolean(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof BooleanNode) {
            return ((BooleanNode) node).getValue();
        }
        throw createWrongTypeException(node, IntegerNode.class);
    }

    public static IllegalStateException createWrongTypeException(Node node, Class<?> clazz) {
        return new IllegalStateException("Expected " + clazz + " but found " + node);
    }

    public static Node getValueNode(Object value) {
        MapNode node = new MapNode(new LinkedHashMap<>());
        if (value instanceof Byte) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.BYTE));
            node.getEntries().put(NodeConstants.VALUE, new IntegerNode((long) value));
        } else if (value instanceof Boolean) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.BOOLEAN));
            node.getEntries().put(NodeConstants.VALUE, BooleanNode.from((Boolean) value));
        } else if (value instanceof Character) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.CHARACTER));
            node.getEntries().put(NodeConstants.VALUE, new IntegerNode((Character) value));
        } else if (value instanceof Short) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.SHORT));
            node.getEntries().put(NodeConstants.VALUE, new IntegerNode((Short) value));
        } else if (value instanceof Integer) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.INTEGER));
            node.getEntries().put(NodeConstants.VALUE, new IntegerNode((Integer) value));
        } else if (value instanceof Long) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.LONG));
            node.getEntries().put(NodeConstants.VALUE, new IntegerNode((Long) value));
        } else if (value instanceof Float) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.FLOAT));
            node.getEntries().put(NodeConstants.VALUE, new FloatNode((Float) value));
        } else if (value instanceof Double) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.DOUBLE));
            node.getEntries().put(NodeConstants.VALUE, new FloatNode((Double) value));
        } else if (value instanceof String) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.STRING));
            node.getEntries().put(NodeConstants.VALUE, new StringNode((String) value));
        } else if (value instanceof Type) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.TYPE));
            node.getEntries().put(NodeConstants.VALUE, new StringNode(((Type) value).getDescriptor()));
        } else if (value instanceof Handle) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.HANDLE));
            node.getEntries().put(NodeConstants.VALUE, getHandleNode((Handle) value));
        } else if (value instanceof ConstantDynamic) {
            node.getEntries().put(NodeConstants.TYPE, new StringNode(NodeConstants.CONSTANT_DYNAMIC));
            node.getEntries().put(NodeConstants.VALUE, getConstantDynamicNode((ConstantDynamic) value));
        } else {
            throw new RuntimeException("Invalid annotation value: " + value);
        }

        return node;
    }

    public static Object fromValueNode(Node node) {
        String type = getAsString(node, NodeConstants.TYPE);
        Node valueNode = asMap(node).getEntries().get(NodeConstants.VALUE);

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
                throw new RuntimeException("Invalid value type: " + valueNode);
        }
    }

    public static ListNode getValueListNode(Object[] values) {
        ListNode argumentsNode = new ListNode(new ArrayList<>());

        for (Object value : values) {
            argumentsNode.getEntries().add(getValueNode(value));
        }

        return argumentsNode;
    }

    public static MapNode getHandleNode(Handle handle) {
        MapNode handleNode = new MapNode(new LinkedHashMap<>());
        handleNode.getEntries().put(NodeConstants.TAG, new IntegerNode(handle.getTag()));
        handleNode.getEntries().put(NodeConstants.OWNER, new StringNode(handle.getOwner()));
        handleNode.getEntries().put(NodeConstants.NAME, new StringNode(handle.getName()));
        handleNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(handle.getDesc()));
        handleNode.getEntries().put(NodeConstants.IS_INTERFACE, BooleanNode.from(handle.isInterface()));
        return handleNode;
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
        MapNode constDynamicNode = new MapNode(new LinkedHashMap<>());
        constDynamicNode.getEntries().put(NodeConstants.NAME, new StringNode(constantDynamic.getName()));
        constDynamicNode.getEntries().put(NodeConstants.DESCRIPTOR, new StringNode(constantDynamic.getDescriptor()));
        constDynamicNode.getEntries().put(NodeConstants.HANDLE, getHandleNode(constantDynamic.getBootstrapMethod()));

        Object[] arguments = new Object[constantDynamic.getBootstrapMethodArgumentCount()];
        for (int i = 0; i < arguments.length; i++) {
            arguments[i] = constantDynamic.getBootstrapMethodArgument(i);
        }
        constDynamicNode.getEntries().put(NodeConstants.ARGUMENTS, getValueListNode(arguments));

        return constDynamicNode;
    }

    public static ConstantDynamic asConstantDynamic(Node node) {
        String name = getAsString(node, NodeConstants.NAME);
        String descriptor = getAsString(node, NodeConstants.DESCRIPTOR);
        Handle handle = asHandle(getAsMap(node, NodeConstants.HANDLE));

        ListNode arguments = getAsList(node, NodeConstants.ARGUMENTS);
        Object[] args = new Object[arguments.getEntries().size()];
        int i = 0;
        for (Node entry : arguments.getEntries()) {
            args[i++] = fromValueNode(entry);
        }

        return new ConstantDynamic(name, descriptor, handle, args);
    }
}
