package com.example.msf_ai.service.impl;

import com.example.msf_ai.dto.ChatMessageDto;
import com.example.msf_ai.dto.ChatRequestDto;
import com.example.msf_ai.dto.ChatResponseDto;
import com.example.msf_ai.dto.LlmTestResponseDto;
import com.example.msf_ai.dto.McpConfigDto;
import com.example.msf_ai.service.interfaces.LlmProviderService;
import com.example.msf_ai.service.interfaces.McpClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

@Service
public class ClaudeAiServiceImpl implements LlmProviderService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiServiceImpl.class);

    private final McpClientService mcpClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    private static final String CLAUDE_BASE_URL = "https://api.anthropic.com/v1";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private static final List<String> DEFAULT_MODELS = List.of(
            "claude-3-5-sonnet-20241022",
            "claude-3-5-haiku-20241022",
            "claude-3-opus-20240229",
            "claude-3-haiku-20240307"
    );

    public ClaudeAiServiceImpl(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @Override
    public boolean supports(String provider) {
        return "claude".equalsIgnoreCase(provider) || "anthropic".equalsIgnoreCase(provider);
    }

    @Override
    public List<String> fetchAvailableModels(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return DEFAULT_MODELS;
        }

        try {
            String rawResponse = restClient.get()
                    .uri(CLAUDE_BASE_URL + "/models")
                    .header("x-api-key", apiKey.trim())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .retrieve()
                    .body(String.class);

            if (rawResponse != null) {
                JsonNode root = objectMapper.readTree(rawResponse);
                JsonNode dataNode = root.path("data");
                if (dataNode.isArray() && !dataNode.isEmpty()) {
                    List<String> list = new ArrayList<>();
                    for (JsonNode item : dataNode) {
                        String id = item.path("id").asText("");
                        if (!id.isBlank()) {
                            list.add(id);
                        }
                    }
                    if (!list.isEmpty()) {
                        return list;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[CLAUDE-AI] Failed to fetch live models from Anthropic: {}", e.getMessage());
        }

        return DEFAULT_MODELS;
    }

    @Override
    public LlmTestResponseDto testLlmConnection(String apiKey, String model) {
        return testConnection(apiKey, model);
    }

    public LlmTestResponseDto testConnection(String apiKey, String model) {
        List<String> availableModels = fetchAvailableModels(apiKey);

        if (apiKey == null || apiKey.isBlank()) {
            return new LlmTestResponseDto(false, "API Key tidak boleh kosong", "claude", model, availableModels);
        }

        String targetModel = (model != null && !model.isBlank()) ? model.trim() : DEFAULT_MODELS.get(0);

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", targetModel);
            payload.put("max_tokens", 15);

            ArrayNode messages = payload.putArray("messages");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", "Halo, test koneksi");

            String rawResponse = restClient.post()
                    .uri(CLAUDE_BASE_URL + "/messages")
                    .header("x-api-key", apiKey.trim())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload.toString())
                    .retrieve()
                    .body(String.class);

            if (rawResponse != null && (rawResponse.contains("content") || rawResponse.contains("id"))) {
                return new LlmTestResponseDto(
                        true,
                        "Koneksi ke Anthropic Claude berhasil! Model '" + targetModel + "' siap digunakan.",
                        "claude",
                        targetModel,
                        availableModels
                );
            } else {
                return new LlmTestResponseDto(
                        false,
                        "Respon dari Anthropic Claude tidak sesuai format yang diharapkan.",
                        "claude",
                        targetModel,
                        availableModels
                );
            }
        } catch (Exception e) {
            String errorDetail = extractErrorMessage(e);
            log.warn("[CLAUDE-AI] Test connection failed for model '{}': {}", targetModel, errorDetail);
            return new LlmTestResponseDto(
                    false,
                    "Koneksi gagal pada Anthropic Claude (Model '" + targetModel + "'): " + errorDetail,
                    "claude",
                    targetModel,
                    availableModels
            );
        }
    }

    @Override
    public ChatResponseDto chat(ChatRequestDto request, String apiKey, String model, List<McpConfigDto> activeMcps, String bearerToken) {
        String targetModel = (model != null && !model.isBlank()) ? model.trim() : DEFAULT_MODELS.get(0);
        List<String> toolsUsed = new ArrayList<>();

        log.info("[CLAUDE-AI] Processing chat request for model: {}", targetModel);

        // 1. Build Tools array for Claude
        Map<String, String> toolToMcpUrlMap = new HashMap<>();
        ArrayNode toolsNode = objectMapper.createArrayNode();

        if (activeMcps != null) {
            for (McpConfigDto mcp : activeMcps) {
                try {
                    List<JsonNode> tools = mcpClientService.fetchAvailableTools(mcp.getUrl(), bearerToken);
                    for (JsonNode tool : tools) {
                        String toolName = tool.path("name").asText();
                        if (!toolName.isBlank()) {
                            toolToMcpUrlMap.put(toolName, mcp.getUrl());

                            ObjectNode toolItem = toolsNode.addObject();
                            toolItem.put("name", toolName);
                            toolItem.put("description", tool.path("description").asText(""));
                            JsonNode schema = tool.path("inputSchema");
                            if (schema != null && !schema.isMissingNode()) {
                                toolItem.set("input_schema", schema);
                            } else {
                                ObjectNode emptySchema = objectMapper.createObjectNode();
                                emptySchema.put("type", "object");
                                toolItem.set("input_schema", emptySchema);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("[CLAUDE-AI] Failed to fetch tools from active MCP '{}': {}", mcp.getName(), e.getMessage());
                }
            }
        }

        // 2. Build Messages payload
        ArrayNode messagesNode = objectMapper.createArrayNode();

        if (request.getConversationHistory() != null) {
            for (ChatMessageDto msg : request.getConversationHistory()) {
                ObjectNode turn = messagesNode.addObject();
                String role = "user".equalsIgnoreCase(msg.getRole()) ? "user" : "assistant";
                turn.put("role", role);
                turn.put("content", msg.getContent());
            }
        }

        ObjectNode currentMsg = messagesNode.addObject();
        currentMsg.put("role", "user");
        currentMsg.put("content", request.getMessage());

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", targetModel);
        payload.put("max_tokens", 4096);
        payload.put("system",
                "Anda adalah asisten AI cerdas untuk sistem manajemen sekolah dan pengguna. " +
                "Gunakan tool MCP yang tersedia saat diminta untuk mengambil atau memverifikasi data siswa atau pengguna. " +
                "Jawab dengan ramah, akurat, dan dalam Bahasa Indonesia yang profesional.");
        payload.set("messages", messagesNode);

        if (!toolsNode.isEmpty()) {
            payload.set("tools", toolsNode);
        }

        try {
            // First call to Claude
            String rawResponse = restClient.post()
                    .uri(CLAUDE_BASE_URL + "/messages")
                    .header("x-api-key", apiKey.trim())
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode responseRoot = objectMapper.readTree(rawResponse);
            JsonNode contentBlocks = responseRoot.path("content");

            // Look for tool_use in response content
            List<JsonNode> toolUseBlocks = new ArrayList<>();
            if (contentBlocks.isArray()) {
                for (JsonNode block : contentBlocks) {
                    if ("tool_use".equalsIgnoreCase(block.path("type").asText())) {
                        toolUseBlocks.add(block);
                    }
                }
            }

            if (!toolUseBlocks.isEmpty()) {
                // Append assistant turn
                ObjectNode assistantTurn = messagesNode.addObject();
                assistantTurn.put("role", "assistant");
                assistantTurn.set("content", contentBlocks);

                // Build user turn with tool results
                ObjectNode userResultTurn = messagesNode.addObject();
                userResultTurn.put("role", "user");
                ArrayNode resultBlocks = userResultTurn.putArray("content");

                for (JsonNode toolUse : toolUseBlocks) {
                    String toolCallId = toolUse.path("id").asText();
                    String functionName = toolUse.path("name").asText();
                    JsonNode inputNode = toolUse.path("input");

                    log.info("[CLAUDE-AI] Claude requested tool '{}' with input: {}", functionName, inputNode);
                    toolsUsed.add(functionName);

                    Map<String, Object> arguments = objectMapper.convertValue(inputNode, Map.class);
                    if (arguments == null) arguments = new HashMap<>();

                    String mcpUrl = toolToMcpUrlMap.get(functionName);
                    if (mcpUrl == null && activeMcps != null && !activeMcps.isEmpty()) {
                        mcpUrl = activeMcps.get(0).getUrl();
                    }

                    JsonNode toolResult = mcpClientService.executeToolCall(mcpUrl, functionName, arguments, bearerToken);
                    log.info("[CLAUDE-AI] Tool '{}' result: {}", functionName, toolResult);

                    ObjectNode resultBlock = resultBlocks.addObject();
                    resultBlock.put("type", "tool_result");
                    resultBlock.put("tool_use_id", toolCallId);
                    if (toolResult != null && toolResult.has("result")) {
                        resultBlock.put("content", toolResult.get("result").toString());
                    } else {
                        resultBlock.put("content", toolResult != null ? toolResult.toString() : "{}");
                    }
                }

                // Second call to Claude to formulate final response
                payload.set("messages", messagesNode);
                String secondResponse = restClient.post()
                        .uri(CLAUDE_BASE_URL + "/messages")
                        .header("x-api-key", apiKey.trim())
                        .header("anthropic-version", ANTHROPIC_VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload.toString())
                        .retrieve()
                        .body(String.class);

                JsonNode secondRoot = objectMapper.readTree(secondResponse);
                String finalReply = extractTextFromClaude(secondRoot.path("content"));
                return new ChatResponseDto(finalReply, toolsUsed, true, targetModel);

            } else {
                String reply = extractTextFromClaude(contentBlocks);
                return new ChatResponseDto(reply, toolsUsed, false, targetModel);
            }

        } catch (Exception e) {
            log.error("[CLAUDE-AI] Error communicating with Claude API: {}", e.getMessage(), e);
            return new ChatResponseDto(
                    "Terjadi kendala saat berkomunikasi dengan Anthropic Claude: " + e.getMessage(),
                    toolsUsed,
                    false,
                    targetModel
            );
        }
    }

    private String extractTextFromClaude(JsonNode contentBlocks) {
        if (contentBlocks != null && contentBlocks.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode block : contentBlocks) {
                if ("text".equalsIgnoreCase(block.path("type").asText())) {
                    sb.append(block.path("text").asText());
                }
            }
            if (!sb.isEmpty()) {
                return sb.toString();
            }
        }
        return "Tidak ada respon teks dari Anthropic Claude.";
    }

    private String extractErrorMessage(Exception e) {
        if (e instanceof RestClientResponseException resEx) {
            try {
                JsonNode errNode = objectMapper.readTree(resEx.getResponseBodyAsString());
                if (errNode.has("error")) {
                    JsonNode err = errNode.get("error");
                    if (err.isObject() && err.has("message")) {
                        return err.get("message").asText();
                    }
                    return err.asText();
                }
            } catch (Exception ignored) {}
            return resEx.getStatusText() + " (" + resEx.getStatusCode().value() + ")";
        }
        return e.getMessage() != null ? e.getMessage() : e.toString();
    }
}
