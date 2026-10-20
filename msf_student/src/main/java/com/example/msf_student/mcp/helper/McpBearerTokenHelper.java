package com.example.msf_student.mcp.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Helper for reading and resolving the Bearer token for MCP tools.
 * Resolves token from:
 * 1. McpContextHolder (captured from inbound HTTP request headers e.g. from Antigravity)
 * 2. Configuration file fallback (supporting Antigravity mcpServers format and flat bearerToken format)
 */
@Component
public class McpBearerTokenHelper {

    private static final Logger log = LoggerFactory.getLogger(McpBearerTokenHelper.class);

    private static final List<String> CANDIDATE_CONFIG_PATHS = List.of(
            System.getProperty("user.home") + File.separator + ".gemini" + File.separator + "config" + File.separator + "mcp_config.json",
            System.getProperty("user.home") + File.separator + ".gemini" + File.separator + "antigravity" + File.separator + "mcp_config.json",
            System.getProperty("user.home") + File.separator + ".gemini" + File.separator + "mcp_config.json"
    );

    private final ObjectMapper objectMapper;
    private final String customConfigPath;

    public McpBearerTokenHelper() {
        this.objectMapper = new ObjectMapper();
        this.customConfigPath = null;
    }

    public McpBearerTokenHelper(String customConfigPath) {
        this.objectMapper = new ObjectMapper();
        this.customConfigPath = customConfigPath;
    }

    /**
     * Resolves the bearer token from HTTP context first, then configuration files.
     *
     * @return normalized Bearer token string (e.g. "Bearer eyJ..."), or empty string if not found
     */
    public String getBearerToken() {
        // 1. Check if token was provided in the current/recent HTTP request context
        String contextToken = McpContextHolder.getToken();
        if (contextToken != null && !contextToken.isBlank()) {
            log.debug("[MCP-HELPER] Bearer token resolved from McpContextHolder.");
            return normalizeBearer(contextToken);
        }

        // 2. Fallback: Read from config file(s)
        if (customConfigPath != null) {
            String token = readTokenFromFile(new File(customConfigPath));
            if (!token.isBlank()) return token;
        }

        for (String pathStr : CANDIDATE_CONFIG_PATHS) {
            File file = new File(pathStr);
            if (file.exists()) {
                String token = readTokenFromFile(file);
                if (!token.isBlank()) {
                    return token;
                }
            }
        }

        log.warn("[MCP-HELPER] No Bearer token could be resolved from HTTP headers or config files.");
        return "";
    }

    private String readTokenFromFile(File file) {
        try {
            if (!file.exists()) {
                return "";
            }
            JsonNode root = objectMapper.readTree(file);

            // Format A: { "bearerToken": "..." }
            JsonNode flatToken = root.get("bearerToken");
            if (flatToken != null && !flatToken.isNull() && !flatToken.asText().isBlank()) {
                log.info("[MCP-HELPER] Bearer token loaded from flat 'bearerToken' in {}", file.getName());
                return normalizeBearer(flatToken.asText().trim());
            }

            // Format B: Antigravity / Claude config { "mcpServers": { "mcp-msf-student": { "headers": { "Authorization": "Bearer ..." } } } }
            JsonNode mcpServers = root.get("mcpServers");
            if (mcpServers != null && mcpServers.isObject()) {
                String token = extractAuthFromServersNode(mcpServers);
                if (!token.isBlank()) {
                    log.info("[MCP-HELPER] Bearer token loaded from 'mcpServers' in {}", file.getName());
                    return normalizeBearer(token);
                }
            }

            // Format C: Root level { "mcp-msf-student": { "headers": { "Authorization": "Bearer ..." } } }
            String token = extractAuthFromServersNode(root);
            if (!token.isBlank()) {
                log.info("[MCP-HELPER] Bearer token loaded from server config in {}", file.getName());
                return normalizeBearer(token);
            }

        } catch (IOException e) {
            log.warn("[MCP-HELPER] Error reading config file {}: {}", file.getAbsolutePath(), e.getMessage());
        }
        return "";
    }

    private String extractAuthFromServersNode(JsonNode node) {
        for (String key : List.of("mcp-msf-student", "msf_student", "mcp_msf_student")) {
            JsonNode serverNode = node.get(key);
            if (serverNode != null) {
                String auth = extractAuthFromHeaders(serverNode);
                if (!auth.isBlank()) return auth;
            }
        }

        Iterator<JsonNode> elements = node.elements();
        while (elements.hasNext()) {
            JsonNode item = elements.next();
            String auth = extractAuthFromHeaders(item);
            if (!auth.isBlank()) return auth;
        }
        return "";
    }

    private String extractAuthFromHeaders(JsonNode serverNode) {
        JsonNode headers = serverNode.get("headers");
        if (headers != null && headers.isObject()) {
            JsonNode authNode = headers.get("Authorization");
            if (authNode == null || authNode.isNull()) {
                authNode = headers.get("authorization");
            }
            if (authNode == null || authNode.isNull()) {
                authNode = headers.get("accessToken");
            }
            if (authNode != null && !authNode.isNull() && !authNode.asText().isBlank()) {
                return authNode.asText().trim();
            }
        }
        return "";
    }

    private String normalizeBearer(String token) {
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
