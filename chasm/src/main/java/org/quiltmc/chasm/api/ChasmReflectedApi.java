package org.quiltmc.chasm.api;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.api.util.ClassInfo;
import org.quiltmc.chasm.api.util.Context;
import org.quiltmc.chasm.internal.transformer.ChasmLangTransformer;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.metadata.Metadata;

/**
 * Exposes the rest of chasm API in a reflection-friendly manor. Intended to allow quilt-loader to invoke chasm without
 * hard-depending on it.
 */
public class ChasmReflectedApi {

    private static final MethodType SIGNATURE_CONTEXT_READFILE = MethodType.methodType(byte[].class, String.class);

    /**
     * Creates a new {@link ChasmProcessor}.
     *
     * @param fileReader A {@link MethodHandle} which accepts a single argument, a {@link String} representing the file
     *            path, and returns a byte[] with the file contents, or null. This is {@link Context#readFile(String)}.
     *
     * @return A {@link ChasmProcessor}.
     */
    public static Object createProcessor(MethodHandle fileReader) {
        MethodType type = fileReader.type();
        if (!type.equals(SIGNATURE_CONTEXT_READFILE)) {
            throw new IllegalArgumentException("Expected 'fileReader' to be of signature " + SIGNATURE_CONTEXT_READFILE
                    + ", but received " + type);
        }

        return new ChasmProcessor(new Context() {
            @Override
            public byte @Nullable [] readFile(String path) {
                try {
                    return (byte @Nullable []) fileReader.invokeExact(path);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public @Nullable ClassInfo getClassInfo(String className) {
                byte[] fileBytes = readFile(className.replace('.', '/').concat(".class"));
                return fileBytes != null ? ClassInfo.fromBytes(fileBytes) : null;
            }
        });
    }

    /**
     * Adds a class to be processed. (This calls
     * {@link ChasmProcessor#addClass(byte[], Metadata)}, setting the metadata object to the given metadata object if
     * not null).
     *
     * @param processor A {@link ChasmProcessor}, as returned by {@link #createProcessor(MethodHandle)}.
     * @param classBytes The class file
     * @param metadata Optional metadata object.
     */
    public static void addClass(Object processor, byte[] classBytes, Object metadata) {
        Metadata meta = new Metadata();
        if (metadata != null) {
            meta.put(ExternalMeta.class, new ExternalMeta(metadata));
        }
        ((ChasmProcessor) processor).addClass(classBytes, meta);
    }

    /**
     * Adds a transformer to process classes.
     *
     * @param processor A {@link ChasmProcessor}, as returned by {@link #createProcessor(MethodHandle)}.
     */
    public static void addTransformer(Object processor, String transformerId, Path file) throws IOException {
        ChasmProcessor chasmProcessor = (ChasmProcessor) processor;
        Node node = Node.parse(file);
        Transformer transformer = new ChasmLangTransformer(transformerId, node, chasmProcessor.getContext());
        chasmProcessor.addTransformer(transformer);
    }

    /**
     * Processes all transformers.
     *
     * @param processor A {@link ChasmProcessor}, as returned by {@link #createProcessor(MethodHandle)}.
     * 
     * @return An {@link Iterable} of every resulting class. You should use {@link #getResultType(Object)} and
     *             {@link #getResultBytes(Object)} to retrieve the actual data.
     */
    public static Iterable<?> process(Object processor) {
        return ((ChasmProcessor) processor).process();
    }

    /**
     * Retrieves the {@link Enum#name()} of the {@link ClassResult#getType()}.

     * @param classResult A {@link ClassResult}, as returned by {@link #process(Object)}.
     * @return The type of the result.
     */
    public static String getResultType(Object classResult) {
        return ((ClassResult) classResult).getType().name();
    }

    /**
     * Retrieves {@link ClassResult#getClassBytes()}.
     *
     * @param classResult A {@link ClassResult}, as returned by {@link #process(Object)}.
     * @return The file bytes of the result, if it wasn't "REMOVED"
     * @throws RuntimeException if {@link #getResultType(Object)} is "REMOVED".
     */
    public static byte[] getResultBytes(Object classResult) {
        return ((ClassResult) classResult).getClassBytes();
    }

    /**
     * Retrieves {@link ClassResult#getMetadata()}, but gets the object passed to
     * {@link #addClass(Object, byte[], Object)}.
     *
     * @param classResult A {@link ClassResult}, as returned by {@link #process(Object)}.
     * @return The metadata object that was passed into {@link #addClass(Object, byte[], Object)}.
     */
    public static Object getExternalMeta(Object classResult) {
        ExternalMeta ext = ((ClassResult) classResult).getMetadata().get(ExternalMeta.class);
        return ext != null ? ext.obj : null;
    }

    private static class ExternalMeta {
        public final Object obj;

        public ExternalMeta(Object obj) {
            this.obj = obj;
        }

        @Override
        public String toString() {
            return Objects.toString(obj);
        }
    }
}
