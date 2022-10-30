package org.quiltmc.chasm.lang.internal.render;

public class RenderUtil {
    private RenderUtil() {
    }

    public static String quotify(String text, char quoteChar) {
        return quoteChar + text.replace("\\", "\\\\").replace("" + quoteChar, "\\" + quoteChar) + quoteChar;
    }

    public static String quotifyIdentifierIfNeeded(String identifier, char quoteChar) {
        return needsQuotes(identifier) ? quotify(identifier, quoteChar) : identifier;
    }

    private static boolean needsQuotes(String identifier) {
        if (identifier.isEmpty()) {
            return true;
        }

        if (!isValidIdentifierStartChar(identifier.charAt(0))) {
            return true;
        }

        for (int i = 1; i < identifier.length(); i++) {
            if (!isValidIdentifierChar(identifier.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isValidIdentifierStartChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
    }

    private static boolean isValidIdentifierChar(char c) {
        return isValidIdentifierStartChar(c) || (c >= '0' && c <= '9');
    }
}
