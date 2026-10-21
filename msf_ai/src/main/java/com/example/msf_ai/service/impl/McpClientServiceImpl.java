package com.example.msf_ai.service.impl;

import com.example.msf_ai.dto.McpTestResponseDto;
import com.example.msf_ai.service.interfaces.McpClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

@Service
public class McpClientServiceImpl implements McpClientService {

    private static final Logger log = LoggerFactory.getLogger(McpClientServiceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    @Override
    public McpTestResponseDto testConnection(String mcpUrl, String bearerToken) {
        log.info("[MCP-CLIENT] Testing connection to MCP URL: {}", mcpUrl);
        String normalizedToken = normalizeBearer(bearerToken);

        try {
            // Step 1: Send initialize handshake
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

            String initResponse = restClient.post()
                    .uri(mcpUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, normalizedToken)
                    .body(initPayload.toString())
                    .retrieve()
                    .body(String.class);

            log.debug("[MCP-CLIENT] Initialize response from {}: {}", mcpUrl, initResponse);

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

        try {
            ObjectNode listPayload = objectMapper.createObjectNode();
            listPayload.put("jsonrpc", "2.0");
            listPayload.put("id", 2);
            listPayload.put("method", "tools/list");
            listPayload.putObject("params");

            String responseBody = restClient.post()
                    .uri(mcpUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, normalizedToken)
                    .body(listPayload.toString())
                    .retrieve()
                    .body(String.class);

            if (responseBody != null) {
                JsonNode root = objectMapper.readTree(responseBody);
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
        }
        return List.of();
    }

    @Override
    public JsonNode executeToolCall(String mcpUrl, String toolName, Map<String, Object> arguments, String bearerToken) {
        log.info("[MCP-CLIENT] Executing tool '{}' on MCP URL '{}' with Bearer token relay", toolName, mcpUrl);
        String normalizedToken = normalizeBearer(bearerToken);

        Map<String, Object> finalArgs = new HashMap<>(arguments != null ? arguments : Map.of());
        // Double security relay: provide bearerToken inside tool arguments as well
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

            String responseBody = restClient.post()
                    .uri(mcpUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    // CRUCIAL: Forward UI request Bearer Token to MCP HTTP headers!
                    .header(HttpHeaders.AUTHORIZATION, normalizedToken)
                    .body(callPayload.toString())
                    .retrieve()
                    .body(String.class);

            log.info("[MCP-CLIENT] Tool '{}' execution successful, response received", toolName);
            if (responseBody != null) {
                return objectMapper.readTree(responseBody);
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

    private String normalizeBearer(String token) {
        if (token == null || token.isBlank()) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
