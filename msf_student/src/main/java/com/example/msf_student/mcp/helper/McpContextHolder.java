package com.example.msf_student.mcp.helper;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Holder for MCP request authentication context.
 * Captures Bearer token from incoming HTTP headers and makes it available to MCP tools.
 */
public class McpContextHolder {

    private static final ThreadLocal<String> CONTEXT_TOKEN = new ThreadLocal<>();
    private static final AtomicReference<String> LAST_KNOWN_TOKEN = new AtomicReference<>("");

    public static void setToken(String token) {
        if (token != null && !token.isBlank()) {
            String normalized = normalizeBearer(token);
            CONTEXT_TOKEN.set(normalized);
            LAST_KNOWN_TOKEN.set(normalized);
        }
    }

    public static String getToken() {
        String token = CONTEXT_TOKEN.get();
        if (token != null && !token.isBlank()) {
            return token;
        }
        return LAST_KNOWN_TOKEN.get();
    }

    public static void clear() {
        CONTEXT_TOKEN.remove();
    }

    private static String normalizeBearer(String token) {
        String trimmed = token.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
        }
        if (!trimmed.toLowerCase().startsWith("bearer ")) {
            return "Bearer " + trimmed;
        }
        return trimmed;
    }
}
