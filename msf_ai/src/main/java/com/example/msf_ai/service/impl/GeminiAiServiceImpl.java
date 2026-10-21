package com.example.msf_ai.service.impl;

import com.example.msf_ai.client.MsmAiClient;
import com.example.msf_ai.dto.ChatMessageDto;
import com.example.msf_ai.dto.ChatRequestDto;
import com.example.msf_ai.dto.ChatResponseDto;
import com.example.msf_ai.dto.GeminiTestResponseDto;
import com.example.msf_ai.dto.McpConfigDto;
import com.example.msf_ai.service.interfaces.GeminiAiService;
import com.example.msf_ai.service.interfaces.McpClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.*;

@Service
public class GeminiAiServiceImpl implements GeminiAiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiServiceImpl.class);

    private final MsmAiClient msmAiClient;
    private final McpClientService mcpClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.builder().build();

    @Value("${ai.gemini.default-api-key:}")
    private String defaultApiKey;

    @Value("${ai.gemini.default-model:gemini-1.5-flash}")
    private String defaultModel;

    @Value("${ai.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta/models}")
    private String geminiEndpoint;

    public GeminiAiServiceImpl(MsmAiClient msmAiClient, McpClientService mcpClientService) {
        this.msmAiClient = msmAiClient;
        this.mcpClientService = mcpClientService;
    }

    @Override
    public ChatResponseDto chat(ChatRequestDto request, String bearerToken) {
        log.info("[GEMINI-AI] Processing chat request: '{}'", request.getMessage());

        // 1. Resolve Gemini API Key & Model from msm_ai or defaults
        String apiKey = msmAiClient.getAiConfigValue("gemini_api_key", defaultApiKey, bearerToken);
        String model = msmAiClient.getAiConfigValue("gemini_model", defaultModel, bearerToken);

        // 2. Fetch Active (Plugged-In) MCP Servers
        List<McpConfigDto> activeMcps = msmAiClient.getActiveMcpConfigs(bearerToken);
        log.info("[GEMINI-AI] Active MCP count: {}", activeMcps.size());

        // 3. Discover available tools from each active MCP server
        Map<String, String> toolToMcpUrlMap = new HashMap<>();
        List<ObjectNode> functionDeclarations = new ArrayList<>();

        for (McpConfigDto mcp : activeMcps) {
            try {
                List<JsonNode> tools = mcpClientService.fetchAvailableTools(mcp.getUrl(), bearerToken);
                for (JsonNode tool : tools) {
                    String toolName = tool.path("name").asText();
                    if (!toolName.isBlank()) {
                        toolToMcpUrlMap.put(toolName, mcp.getUrl());
                        ObjectNode funcDecl = convertMcpToolToGeminiFunction(tool);
                        functionDeclarations.add(funcDecl);
                    }
                }
            } catch (Exception e) {
                log.warn("[GEMINI-AI] Failed to fetch tools from active MCP '{}': {}", mcp.getName(), e.getMessage());
            }
        }

        // 4. Build contents payload (multi-turn conversation)
        ArrayNode contentsNode = objectMapper.createArrayNode();

        // Append conversation history if present
        if (request.getConversationHistory() != null) {
            for (ChatMessageDto msg : request.getConversationHistory()) {
                ObjectNode turn = contentsNode.addObject();
                String role = "user".equalsIgnoreCase(msg.getRole()) ? "user" : "model";
                turn.put("role", role);
                ArrayNode parts = turn.putArray("parts");
                parts.addObject().put("text", msg.getContent());
            }
        }

        // Append current user message
        ObjectNode currentTurn = contentsNode.addObject();
        currentTurn.put("role", "user");
        currentTurn.putArray("parts").addObject().put("text", request.getMessage());

        // 5. Build first Gemini API call payload
        ObjectNode geminiPayload = objectMapper.createObjectNode();
        geminiPayload.set("contents", contentsNode);

        // Add System Instruction
        ObjectNode sysInstruction = geminiPayload.putObject("systemInstruction");
        sysInstruction.putArray("parts").addObject().put("text",
                "Anda adalah asisten AI cerdas untuk sistem manajemen sekolah dan pengguna. " +
                "Gunakan tool MCP yang tersedia saat diminta untuk mengambil atau memverifikasi data siswa atau pengguna. " +
                "Jawab dengan ramah, akurat, dan dalam Bahasa Indonesia yang profesional.");

        if (!functionDeclarations.isEmpty()) {
            ArrayNode toolsNode = geminiPayload.putArray("tools");
            ObjectNode toolObj = toolsNode.addObject();
            ArrayNode funcsArray = toolObj.putArray("functionDeclarations");
            for (ObjectNode func : functionDeclarations) {
                funcsArray.add(func);
            }
        }

        List<String> toolsUsed = new ArrayList<>();
        boolean toolExecuted = false;

        try {
            // Send request to Gemini API
            String url = geminiEndpoint + "/" + model + ":generateContent?key=" + apiKey;
            String rawResponse = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(geminiPayload.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode responseRoot = objectMapper.readTree(rawResponse);
            JsonNode candidate = responseRoot.path("candidates").path(0);
            JsonNode content = candidate.path("content");
            JsonNode parts = content.path("parts");

            // Check if Gemini invoked a function call
            JsonNode functionCallPart = null;
            for (JsonNode part : parts) {
                if (part.has("functionCall")) {
                    functionCallPart = part.get("functionCall");
                    break;
                }
            }

            if (functionCallPart != null) {
                String toolName = functionCallPart.path("name").asText();
                JsonNode argsNode = functionCallPart.path("args");
                Map<String, Object> arguments = objectMapper.convertValue(argsNode, Map.class);
                if (arguments == null) arguments = new HashMap<>();

                log.info("[GEMINI-AI] Gemini requested tool call: '{}' with args: {}", toolName, arguments);
                toolsUsed.add(toolName);
                toolExecuted = true;

                String mcpUrl = toolToMcpUrlMap.get(toolName);
                if (mcpUrl == null && !activeMcps.isEmpty()) {
                    mcpUrl = activeMcps.get(0).getUrl();
                }

                // 6. EXECUTE THE TOOL VIA MCP CLIENT CARRYING THE BEARER TOKEN!
                JsonNode toolResultNode = mcpClientService.executeToolCall(mcpUrl, toolName, arguments, bearerToken);
                log.info("[GEMINI-AI] Tool result received: {}", toolResultNode.toString());

                // 7. Append Model Function Call Turn & Function Response Turn
                ObjectNode modelTurn = contentsNode.addObject();
                modelTurn.put("role", "model");
                modelTurn.putArray("parts").add(objectMapper.createObjectNode().set("functionCall", functionCallPart));

                ObjectNode functionTurn = contentsNode.addObject();
                functionTurn.put("role", "tool");
                ArrayNode functionParts = functionTurn.putArray("parts");
                ObjectNode funcResponseWrapper = functionParts.addObject().putObject("functionResponse");
                funcResponseWrapper.put("name", toolName);
                
                // Format response payload for Gemini
                ObjectNode responsePayload = funcResponseWrapper.putObject("response");
                responsePayload.set("result", toolResultNode);

                // 8. Second call to Gemini to synthesize natural language answer
                String secondResponse = restClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(geminiPayload.toString())
                        .retrieve()
                        .body(String.class);

                JsonNode secondRoot = objectMapper.readTree(secondResponse);
                String finalReply = extractTextFromGeminiResponse(secondRoot);
                return new ChatResponseDto(finalReply, toolsUsed, true, model);
            } else {
                // Direct text answer without tool call
                String directReply = extractTextFromGeminiResponse(responseRoot);
                return new ChatResponseDto(directReply, toolsUsed, false, model);
            }

        } catch (Exception e) {
            log.error("[GEMINI-AI] Error communicating with Gemini API: {}", e.getMessage(), e);
            return new ChatResponseDto(
                    "Terjadi kendala saat berkomunikasi dengan LLM Gemini: " + e.getMessage(),
                    toolsUsed,
                    false,
                    model
            );
        }
    }

    @Override
    public boolean testApiKey(String apiKey) {
        return testConnection(apiKey, defaultModel).isSuccess();
    }

    @Override
    public GeminiTestResponseDto testConnection(String apiKey, String model) {
        if (apiKey == null || apiKey.isBlank()) {
            return new GeminiTestResponseDto(false, "API Key tidak boleh kosong", model, Collections.emptyList());
        }

        String targetModel = (model != null && !model.isBlank()) ? model.trim() : defaultModel;
        List<String> availableModels = fetchAvailableModels(apiKey);

        try {
            String testUrl = geminiEndpoint + "/" + targetModel + ":generateContent?key=" + apiKey;
            ObjectNode payload = objectMapper.createObjectNode();
            ArrayNode contents = payload.putArray("contents");
            ObjectNode userTurn = contents.addObject();
            userTurn.put("role", "user");
            userTurn.putArray("parts").addObject().put("text", "Halo, test koneksi");

            String response = restClient.post()
                    .uri(testUrl)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload.toString())
                    .retrieve()
                    .body(String.class);

            if (response != null && response.contains("candidates")) {
                return new GeminiTestResponseDto(
                        true,
                        "Koneksi ke Gemini AI berhasil! Model '" + targetModel + "' siap digunakan.",
                        targetModel,
                        availableModels
                );
            } else {
                return new GeminiTestResponseDto(
                        false,
                        "Format respon Gemini tidak terduga atau tidak mengandung kandidat teks.",
                        targetModel,
                        availableModels
                );
            }
        } catch (Exception e) {
            String errorDetail = extractErrorMessage(e);
            log.warn("[GEMINI-AI] API Key test failed for model '{}': {}", targetModel, errorDetail);
            return new GeminiTestResponseDto(
                    false,
                    "Koneksi gagal pada model '" + targetModel + "': " + errorDetail,
                    targetModel,
                    availableModels
            );
        }
    }

    @Override
    public List<String> fetchAvailableModels(String apiKey) {
        List<String> defaultModels = List.of("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash");
        if (apiKey == null || apiKey.isBlank()) {
            return defaultModels;
        }

        try {
            String listUrl = geminiEndpoint + "?key=" + apiKey;
            String rawResponse = restClient.get()
                    .uri(listUrl)
                    .header("x-goog-api-key", apiKey)
                    .retrieve()
                    .body(String.class);

            if (rawResponse != null) {
                JsonNode root = objectMapper.readTree(rawResponse);
                JsonNode modelsNode = root.path("models");
                if (modelsNode.isArray()) {
                    List<String> foundModels = new ArrayList<>();
                    for (JsonNode m : modelsNode) {
                        String name = m.path("name").asText(""); // e.g. "models/gemini-1.5-flash"
                        if (name.startsWith("models/")) {
                            name = name.substring("models/".length());
                        }
                        boolean supportsGenerate = false;
                        JsonNode methods = m.path("supportedGenerationMethods");
                        if (methods.isArray()) {
                            for (JsonNode method : methods) {
                                if ("generateContent".equalsIgnoreCase(method.asText())) {
                                    supportsGenerate = true;
                                    break;
                                }
                            }
                        }
                        if (supportsGenerate && name.toLowerCase().contains("gemini")) {
                            foundModels.add(name);
                        }
                    }
                    if (!foundModels.isEmpty()) {
                        return foundModels;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[GEMINI-AI] Failed to fetch dynamic models from Google: {}", e.getMessage());
        }
        return defaultModels;
    }

    private String extractErrorMessage(Exception e) {
        if (e instanceof RestClientResponseException resEx) {
            try {
                JsonNode errNode = objectMapper.readTree(resEx.getResponseBodyAsString());
                if (errNode.has("error") && errNode.get("error").has("message")) {
                    return errNode.get("error").get("message").asText();
                }
            } catch (Exception ignored) {}
            return resEx.getStatusText() + " (" + resEx.getStatusCode().value() + ")";
        }
        return e.getMessage() != null ? e.getMessage() : e.toString();
    }

    private ObjectNode convertMcpToolToGeminiFunction(JsonNode mcpTool) {
        ObjectNode func = objectMapper.createObjectNode();
        func.put("name", mcpTool.path("name").asText());
        func.put("description", mcpTool.path("description").asText(""));

        JsonNode inputSchema = mcpTool.path("inputSchema");
        if (inputSchema.isObject()) {
            ObjectNode params = func.putObject("parameters");
            params.put("type", "OBJECT");
            JsonNode properties = inputSchema.path("properties");
            if (properties.isObject()) {
                params.set("properties", properties);
            }
            JsonNode required = inputSchema.path("required");
            if (required.isArray()) {
                params.set("required", required);
            }
        }
        return func;
    }

    private String extractTextFromGeminiResponse(JsonNode root) {
        JsonNode parts = root.path("candidates").path(0).path("content").path("parts");
        if (parts.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode part : parts) {
                if (part.has("text")) {
                    sb.append(part.path("text").asText());
                }
            }
            if (!sb.isEmpty()) {
                return sb.toString();
            }
        }
        return "Tidak ada respon teks dari model.";
    }
}
