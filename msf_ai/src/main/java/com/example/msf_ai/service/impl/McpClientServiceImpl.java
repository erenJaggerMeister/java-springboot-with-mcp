package com.example.msf_ai.service.impl;

import com.example.msf_ai.dto.McpTestResponseDto;
import com.example.msf_ai.service.interfaces.McpClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class McpClientServiceImpl implements McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientServiceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    // Cache active session IDs per MCP URL (required by Spring AI Streamable HTTP transport)
    private final Map<String, String> sessionMap = new ConcurrentHashMap<>();

    @Override
    public McpTestResponseDto testConnection(String mcpUrl, String bearerToken) {
        log.info("[MCP-CLIENT] Testing connection to MCP URL: {}", mcpUrl);
        String normalizedToken = normalizeBearer(bearerToken);

        // Invalidate old session on explicit test connection
        sessionMap.remove(mcpUrl);

        try {
            // Step 1: Send initialize handshake with required Streamable HTTP headers
            ObjectNode initPayload = objectMapper.createObjectNode();
            initPayload.put("jsonrpc", "2.0");
            initPayload.put("id", 1);
            initPayload.put("method", "initialize");
            ObjectNode params = initPayload.putObject("params");
            params.put("protocolVersion", "2024-11-05");
            params.putObject("capabilities");
            ObjectNode clientInfo = params.putObject("clientInfo");
            clientInfo.put("name", "msf_ai_client");
            clientInfo.put("version", "1.0.0");

            ResponseEntity<String> initResponse = sendMcpRequest(mcpUrl, initPayload.toString(), normalizedToken, null);

            // Capture session ID returned by Spring AI MCP Server
            String sessionId = initResponse.getHeaders().getFirst("Mcp-Session-Id");
            if (sessionId != null && !sessionId.isBlank()) {
                sessionMap.put(mcpUrl, sessionId);
                log.info("[MCP-CLIENT] Handshake successful, session ID: {}", sessionId);

                // Send notifications/initialized as per MCP specification
                try {
                    ObjectNode notifyPayload = objectMapper.createObjectNode();
                    notifyPayload.put("jsonrpc", "2.0");
                    notifyPayload.put("method", "notifications/initialized");
                    sendMcpRequest(mcpUrl, notifyPayload.toString(), normalizedToken, sessionId);
                } catch (Exception notifyEx) {
                    log.debug("[MCP-CLIENT] Initialized notification skipped: {}", notifyEx.getMessage());
                }
            }

            log.debug("[MCP-CLIENT] Initialize response body from {}: {}", mcpUrl, initResponse.getBody());

            // Step 2: Query tools/list
            List<JsonNode> tools = fetchAvailableTools(mcpUrl, bearerToken);
            List<String> toolNames = new ArrayList<>();
            for (JsonNode tool : tools) {
                if (tool.has("name")) {
                    toolNames.add(tool.get("name").asText());
                }
            }

            return new McpTestResponseDto(
                    true,
                    "Koneksi MCP Berhasil! Ditemukan " + toolNames.size() + " tool.",
                    toolNames.size(),
                    toolNames
            );
        } catch (Exception e) {
            log.error("[MCP-CLIENT] Failed to connect to MCP at {}: {}", mcpUrl, e.getMessage());
            return new McpTestResponseDto(
                    false,
                    "Gagal terhubung ke MCP: " + e.getMessage(),
                    0,
                    List.of()
            );
        }
    }

    @Override
    public List<JsonNode> fetchAvailableTools(String mcpUrl, String bearerToken) {
        log.info("[MCP-CLIENT] Fetching available tools from: {}", mcpUrl);
        String normalizedToken = normalizeBearer(bearerToken);
        String sessionId = sessionMap.get(mcpUrl);

        if (sessionId == null) {
            sessionId = getOrInitSession(mcpUrl, bearerToken);
        }

        try {
            ObjectNode listPayload = objectMapper.createObjectNode();
            listPayload.put("jsonrpc", "2.0");
            listPayload.put("id", 2);
            listPayload.put("method", "tools/list");
            listPayload.putObject("params");

            ResponseEntity<String> response = sendMcpRequest(mcpUrl, listPayload.toString(), normalizedToken, sessionId);
            String responseBody = response.getBody();

            if (responseBody != null) {
                JsonNode root = parseMcpResponseBody(responseBody);
                JsonNode toolsNode = root.path("result").path("tools");
                if (toolsNode.isArray()) {
                    List<JsonNode> tools = new ArrayList<>();
                    for (JsonNode tool : toolsNode) {
                        tools.add(tool);
                    }
                    log.info("[MCP-CLIENT] Discovered {} tools from {}", tools.size(), mcpUrl);
                    return tools;
                }
            }
        } catch (Exception e) {
            log.warn("[MCP-CLIENT] Error fetching tools from {}: {}", mcpUrl, e.getMessage());

            // Retry once if session expired or missing
            if (sessionId != null) {
                sessionMap.remove(mcpUrl);
                String newSessionId = getOrInitSession(mcpUrl, bearerToken);
                if (newSessionId != null) {
                    try {
                        ObjectNode listPayload = objectMapper.createObjectNode();
                        listPayload.put("jsonrpc", "2.0");
                        listPayload.put("id", 2);
                        listPayload.put("method", "tools/list");
                        listPayload.putObject("params");

                        ResponseEntity<String> retryResponse = sendMcpRequest(mcpUrl, listPayload.toString(), normalizedToken, newSessionId);
                        JsonNode root = parseMcpResponseBody(retryResponse.getBody());
                        JsonNode toolsNode = root.path("result").path("tools");
                        if (toolsNode.isArray()) {
                            List<JsonNode> tools = new ArrayList<>();
                            for (JsonNode tool : toolsNode) {
                                tools.add(tool);
                            }
                            return tools;
                        }
                    } catch (Exception retryEx) {
                        log.warn("[MCP-CLIENT] Retry failed fetching tools from {}: {}", mcpUrl, retryEx.getMessage());
                    }
                }
            }
        }
        return List.of();
    }

    @Override
    public JsonNode executeToolCall(String mcpUrl, String toolName, Map<String, Object> arguments, String bearerToken) {
        log.info("[MCP-CLIENT] Executing tool '{}' on MCP URL '{}' with Bearer token relay", toolName, mcpUrl);
        String normalizedToken = normalizeBearer(bearerToken);
        String sessionId = sessionMap.get(mcpUrl);

        if (sessionId == null) {
            sessionId = getOrInitSession(mcpUrl, bearerToken);
        }

        Map<String, Object> finalArgs = new HashMap<>(arguments != null ? arguments : Map.of());
        if (!finalArgs.containsKey("bearerToken") && !normalizedToken.isBlank()) {
            finalArgs.put("bearerToken", normalizedToken);
        }

        try {
            ObjectNode callPayload = objectMapper.createObjectNode();
            callPayload.put("jsonrpc", "2.0");
            callPayload.put("id", UUID.randomUUID().toString());
            callPayload.put("method", "tools/call");
            ObjectNode params = callPayload.putObject("params");
            params.put("name", toolName);
            params.set("arguments", objectMapper.valueToTree(finalArgs));

            ResponseEntity<String> response = sendMcpRequest(mcpUrl, callPayload.toString(), normalizedToken, sessionId);
            log.info("[MCP-CLIENT] Tool '{}' execution successful, response received", toolName);
            String responseBody = response.getBody();
            if (responseBody != null) {
                return parseMcpResponseBody(responseBody);
            }
        } catch (Exception e) {
            log.error("[MCP-CLIENT] Error executing tool '{}' on {}: {}", toolName, mcpUrl, e.getMessage());
            ObjectNode errorNode = objectMapper.createObjectNode();
            errorNode.put("isError", true);
            errorNode.put("error", e.getMessage());
            return errorNode;
        }

        return objectMapper.createObjectNode();
    }

    /**
     * Executes HTTP POST to MCP endpoint with mandatory Streamable HTTP headers:
     * - Content-Type: application/json
     * - Accept: text/event-stream, application/json (mandatory for Spring AI MCP Server)
     * - Authorization: Bearer token (if present)
     * - Mcp-Session-Id: session ID (if established)
     */
    private ResponseEntity<String> sendMcpRequest(String mcpUrl, String payload, String normalizedToken, String sessionId) {
        RestClient.RequestBodySpec spec = restClient.post()
                .uri(mcpUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM, MediaType.APPLICATION_JSON);

        if (normalizedToken != null && !normalizedToken.isBlank()) {
            spec.header(HttpHeaders.AUTHORIZATION, normalizedToken);
        }
        if (sessionId != null && !sessionId.isBlank()) {
            spec.header("Mcp-Session-Id", sessionId);
        }

        return spec.body(payload).retrieve().toEntity(String.class);
    }

    /**
     * Initiates handshake with the MCP server to obtain a session ID.
     */
    private synchronized String getOrInitSession(String mcpUrl, String bearerToken) {
        String existing = sessionMap.get(mcpUrl);
        if (existing != null) {
            return existing;
        }

        String normalizedToken = normalizeBearer(bearerToken);
        try {
            ObjectNode initPayload = objectMapper.createObjectNode();
            initPayload.put("jsonrpc", "2.0");
            initPayload.put("id", 1);
            initPayload.put("method", "initialize");
            ObjectNode params = initPayload.putObject("params");
            params.put("protocolVersion", "2024-11-05");
            params.putObject("capabilities");
            ObjectNode clientInfo = params.putObject("clientInfo");
            clientInfo.put("name", "msf_ai_client");
            clientInfo.put("version", "1.0.0");

            ResponseEntity<String> response = sendMcpRequest(mcpUrl, initPayload.toString(), normalizedToken, null);
            String sessionId = response.getHeaders().getFirst("Mcp-Session-Id");
            if (sessionId != null && !sessionId.isBlank()) {
                sessionMap.put(mcpUrl, sessionId);

                try {
                    ObjectNode notifyPayload = objectMapper.createObjectNode();
                    notifyPayload.put("jsonrpc", "2.0");
                    notifyPayload.put("method", "notifications/initialized");
                    sendMcpRequest(mcpUrl, notifyPayload.toString(), normalizedToken, sessionId);
                } catch (Exception ignored) {}

                return sessionId;
            }
        } catch (Exception e) {
            log.error("[MCP-CLIENT] Failed to initialize session with {}: {}", mcpUrl, e.getMessage());
        }
        return null;
    }

    /**
     * Parses responses which can be either standard JSON or Server-Sent Events (SSE).
     * Spring AI MCP Server returns tools/list and tools/call as SSE text/event-stream format.
     */
    private JsonNode parseMcpResponseBody(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return objectMapper.createObjectNode();
        }
        String trimmed = responseBody.trim();

        // 1. Direct JSON response
        if (trimmed.startsWith("{")) {
            try {
                return objectMapper.readTree(trimmed);
            } catch (Exception ignored) {}
        }

        // 2. Server-Sent Events (SSE) format: 'data: {...}'
        StringBuilder dataAccumulator = new StringBuilder();
        for (String line : trimmed.split("\r?\n")) {
            if (line.startsWith("data:")) {
                dataAccumulator.append(line.substring(5).trim());
            }
        }
        if (dataAccumulator.length() > 0) {
            try {
                return objectMapper.readTree(dataAccumulator.toString());
            } catch (Exception e) {
                log.warn("[MCP-CLIENT] Could not parse SSE data payload as JSON: {}", e.getMessage());
            }
        }

        // 3. Fallback: extract between first '{' and last '}'
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start != -1 && end > start) {
            try {
                return objectMapper.readTree(trimmed.substring(start, end + 1));
            } catch (Exception ignored) {}
        }

        return objectMapper.createObjectNode();
    }

    private String normalizeBearer(String token) {
        if (token == null || token.isBlank()) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
