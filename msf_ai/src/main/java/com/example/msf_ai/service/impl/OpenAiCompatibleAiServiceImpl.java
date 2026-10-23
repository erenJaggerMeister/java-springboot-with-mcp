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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

@Service
public class OpenAiCompatibleAiServiceImpl implements LlmProviderService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompatibleAiServiceImpl.class);

    private final McpClientService mcpClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    private static final Map<String, String> PROVIDER_BASE_URLS = Map.of(
            "openai", "https://api.openai.com/v1",
            "deepseek", "https://api.deepseek.com",
            "mistral", "https://api.mistral.ai/v1",
            "nvidia", "https://integrate.api.nvidia.com/v1",
            "openrouter", "https://openrouter.ai/api/v1"
    );

    private static final Map<String, List<String>> DEFAULT_MODELS = Map.of(
            "openai", List.of("gpt-4o", "gpt-4o-mini", "o1", "o1-mini", "gpt-4-turbo", "gpt-3.5-turbo"),
            "deepseek", List.of("deepseek-chat", "deepseek-reasoner"),
            "mistral", List.of("mistral-large-latest", "mistral-medium-latest", "mistral-small-latest", "codestral-latest", "open-mistral-nemo"),
            "nvidia", List.of("meta/llama-3.1-70b-instruct", "meta/llama-3.1-8b-instruct", "mistralai/mixtral-8x22b-instruct", "nvidia/nemotron-4-340b-instruct"),
            "openrouter", List.of("openai/gpt-4o-mini", "openai/gpt-4o", "anthropic/claude-3.5-sonnet", "deepseek/deepseek-chat", "deepseek/deepseek-r1", "google/gemini-2.0-flash-exp:free", "google/gemini-flash-1.5", "meta-llama/llama-3.3-70b-instruct")
    );

    public OpenAiCompatibleAiServiceImpl(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @Override
    public boolean supports(String provider) {
        if (provider == null) return false;
        String lower = provider.trim().toLowerCase();
        return PROVIDER_BASE_URLS.containsKey(lower);
    }

    public List<String> fetchAvailableModels(String apiKey, String provider) {
        String lowerProvider = provider != null ? provider.trim().toLowerCase() : "openai";
        List<String> defaultList = DEFAULT_MODELS.getOrDefault(lowerProvider, DEFAULT_MODELS.get("openai"));

        if (apiKey == null || apiKey.isBlank()) {
            return defaultList;
        }

        String baseUrl = PROVIDER_BASE_URLS.get(lowerProvider);
        if (baseUrl == null) return defaultList;

        try {
            var getReq = restClient.get()
                    .uri(baseUrl + "/models")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim());
            if ("openrouter".equals(lowerProvider)) {
                getReq.header("HTTP-Referer", "http://localhost:8080")
                      .header("X-Title", "MSF AI Assistant");
            }
            String rawResponse = getReq.retrieve().body(String.class);

            if (rawResponse != null) {
                JsonNode root = objectMapper.readTree(rawResponse);
                JsonNode dataNode = root.path("data");
                if (dataNode.isArray() && !dataNode.isEmpty()) {
                    List<String> dynamicModels = new ArrayList<>();
                    for (JsonNode item : dataNode) {
                        String id = item.path("id").asText("");
                        if (!id.isBlank()) {
                            dynamicModels.add(id);
                        }
                    }
                    if (!dynamicModels.isEmpty()) {
                        if ("openrouter".equals(lowerProvider)) {
                            Set<String> ordered = new LinkedHashSet<>(defaultList);
                            ordered.addAll(dynamicModels);
                            return new ArrayList<>(ordered);
                        }
                        return dynamicModels;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[OPENAI-COMPAT] Failed to fetch live models for provider '{}': {}", lowerProvider, e.getMessage());
        }

        return defaultList;
    }

    @Override
    public List<String> fetchAvailableModels(String apiKey) {
        return fetchAvailableModels(apiKey, "openai");
    }

    @Override
    public LlmTestResponseDto testLlmConnection(String apiKey, String model) {
        return testConnection(apiKey, model, "openai");
    }

    public LlmTestResponseDto testConnection(String apiKey, String model, String provider) {
        String lowerProvider = provider != null ? provider.trim().toLowerCase() : "openai";
        List<String> availableModels = fetchAvailableModels(apiKey, lowerProvider);

        if (apiKey == null || apiKey.isBlank()) {
            return new LlmTestResponseDto(false, "API Key tidak boleh kosong", lowerProvider, model, availableModels);
        }

        String targetModel = (model != null && !model.isBlank()) ? model.trim() : availableModels.get(0);
        String baseUrl = PROVIDER_BASE_URLS.get(lowerProvider);

        if (baseUrl == null) {
            return new LlmTestResponseDto(false, "Provider '" + provider + "' tidak didukung", lowerProvider, targetModel, availableModels);
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("model", targetModel);
            payload.put("max_tokens", 15);

            ArrayNode messages = payload.putArray("messages");
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", "Halo, test koneksi");

            var postReq = restClient.post()
                    .uri(baseUrl + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload.toString());
            if ("openrouter".equals(lowerProvider)) {
                postReq.header("HTTP-Referer", "http://localhost:8080")
                       .header("X-Title", "MSF AI Assistant");
            }
            String rawResponse = postReq.retrieve().body(String.class);

            if (rawResponse != null && (rawResponse.contains("choices") || rawResponse.contains("created"))) {
                return new LlmTestResponseDto(
                        true,
                        "Koneksi ke " + lowerProvider.toUpperCase() + " berhasil! Model '" + targetModel + "' siap digunakan.",
                        lowerProvider,
                        targetModel,
                        availableModels
                );
            } else {
                return new LlmTestResponseDto(
                        false,
                        "Respon dari " + lowerProvider + " tidak sesuai format yang diharapkan.",
                        lowerProvider,
                        targetModel,
                        availableModels
                );
            }
        } catch (Exception e) {
            String errorDetail = extractErrorMessage(e);
            log.warn("[OPENAI-COMPAT] Test connection failed for provider '{}' model '{}': {}", lowerProvider, targetModel, errorDetail);
            return new LlmTestResponseDto(
                    false,
                    "Koneksi gagal pada " + lowerProvider.toUpperCase() + " (Model '" + targetModel + "'): " + errorDetail,
                    lowerProvider,
                    targetModel,
                    availableModels
            );
        }
    }

    public LlmTestResponseDto testConnection(String apiKey, String model) {
        return testConnection(apiKey, model, "openai");
    }

    public ChatResponseDto chat(ChatRequestDto request, String apiKey, String model, List<McpConfigDto> activeMcps, String bearerToken, String provider) {
        String lowerProvider = provider != null ? provider.trim().toLowerCase() : "openai";
        String baseUrl = PROVIDER_BASE_URLS.getOrDefault(lowerProvider, PROVIDER_BASE_URLS.get("openai"));
        List<String> toolsUsed = new ArrayList<>();

        log.info("[{}] Processing chat request for model: {}", lowerProvider.toUpperCase(), model);

        // 1. Discover available tools from active MCPs
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
                            toolItem.put("type", "function");
                            ObjectNode funcObj = toolItem.putObject("function");
                            funcObj.put("name", toolName);
                            funcObj.put("description", tool.path("description").asText(""));
                            JsonNode schema = tool.path("inputSchema");
                            if (schema != null && !schema.isMissingNode()) {
                                funcObj.set("parameters", schema);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("[{}] Failed to fetch tools from active MCP '{}': {}", lowerProvider.toUpperCase(), mcp.getName(), e.getMessage());
                }
            }
        }

        // 2. Build conversation payload
        ArrayNode messagesNode = objectMapper.createArrayNode();

        // System message
        ObjectNode systemMsg = messagesNode.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content",
                "Anda adalah asisten AI cerdas untuk sistem manajemen sekolah dan pengguna. " +
                "Gunakan tool MCP yang tersedia saat diminta untuk mengambil atau memverifikasi data siswa atau pengguna. " +
                "Jawab dengan ramah, akurat, dan dalam Bahasa Indonesia yang profesional.");

        // Conversation history
        if (request.getConversationHistory() != null) {
            for (ChatMessageDto msg : request.getConversationHistory()) {
                ObjectNode turn = messagesNode.addObject();
                String role = "user".equalsIgnoreCase(msg.getRole()) ? "user" : "assistant";
                turn.put("role", role);
                turn.put("content", msg.getContent());
            }
        }

        // Current message
        ObjectNode currentMsg = messagesNode.addObject();
        currentMsg.put("role", "user");
        currentMsg.put("content", request.getMessage());

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("model", model);
        payload.set("messages", messagesNode);

        if (!toolsNode.isEmpty()) {
            payload.set("tools", toolsNode);
            payload.put("tool_choice", "auto");
        }

        try {
            // First call to LLM
            var postReq = restClient.post()
                    .uri(baseUrl + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload.toString());
            if ("openrouter".equals(lowerProvider)) {
                postReq.header("HTTP-Referer", "http://localhost:8080")
                       .header("X-Title", "MSF AI Assistant");
            }
            String rawResponse = postReq.retrieve().body(String.class);

            JsonNode responseRoot = objectMapper.readTree(rawResponse);
            JsonNode choice = responseRoot.path("choices").path(0);
            JsonNode choiceMessage = choice.path("message");

            // Check for tool calls
            JsonNode toolCalls = choiceMessage.path("tool_calls");
            if (toolCalls.isArray() && !toolCalls.isEmpty()) {
                // Add assistant response message with tool_calls
                messagesNode.add(choiceMessage);

                // Execute each tool call via MCP
                for (JsonNode toolCall : toolCalls) {
                    String callId = toolCall.path("id").asText();
                    String functionName = toolCall.path("function").path("name").asText();
                    String rawArgs = toolCall.path("function").path("arguments").asText("{}");

                    log.info("[{}] LLM requested tool '{}' with args: {}", lowerProvider.toUpperCase(), functionName, rawArgs);
                    toolsUsed.add(functionName);

                    Map<String, Object> arguments = new HashMap<>();
                    try {
                        arguments = objectMapper.readValue(rawArgs, Map.class);
                    } catch (Exception ex) {
                        log.warn("Failed to parse tool call arguments as JSON: {}", ex.getMessage());
                    }

                    String mcpUrl = toolToMcpUrlMap.get(functionName);
                    if (mcpUrl == null && activeMcps != null && !activeMcps.isEmpty()) {
                        mcpUrl = activeMcps.get(0).getUrl();
                    }

                    JsonNode toolResult = mcpClientService.executeToolCall(mcpUrl, functionName, arguments, bearerToken);
                    log.info("[{}] Tool '{}' result: {}", lowerProvider.toUpperCase(), functionName, toolResult);

                    // Append tool response
                    ObjectNode toolMsg = messagesNode.addObject();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", callId);
                    if (toolResult != null && toolResult.has("result")) {
                        toolMsg.put("content", toolResult.get("result").toString());
                    } else {
                        toolMsg.put("content", toolResult != null ? toolResult.toString() : "{}");
                    }
                }

                // Second call to LLM to synthesize natural answer
                payload.set("messages", messagesNode);
                var postReq2 = restClient.post()
                        .uri(baseUrl + "/chat/completions")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey.trim())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(payload.toString());
                if ("openrouter".equals(lowerProvider)) {
                    postReq2.header("HTTP-Referer", "http://localhost:8080")
                             .header("X-Title", "MSF AI Assistant");
                }
                String secondResponse = postReq2.retrieve().body(String.class);

                JsonNode secondRoot = objectMapper.readTree(secondResponse);
                String finalReply = secondRoot.path("choices").path(0).path("message").path("content").asText("");
                return new ChatResponseDto(finalReply, toolsUsed, true, model);
            } else {
                // Direct response without tools
                String reply = choiceMessage.path("content").asText("");
                return new ChatResponseDto(reply, toolsUsed, false, model);
            }

        } catch (Exception e) {
            log.error("[{}] Error communicating with API: {}", lowerProvider.toUpperCase(), e.getMessage(), e);
            return new ChatResponseDto(
                    "Terjadi kendala saat berkomunikasi dengan LLM " + lowerProvider.toUpperCase() + ": " + e.getMessage(),
                    toolsUsed,
                    false,
                    model
            );
        }
    }

    @Override
    public ChatResponseDto chat(ChatRequestDto request, String apiKey, String model, List<McpConfigDto> activeMcps, String bearerToken) {
        return chat(request, apiKey, model, activeMcps, bearerToken, "openai");
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
