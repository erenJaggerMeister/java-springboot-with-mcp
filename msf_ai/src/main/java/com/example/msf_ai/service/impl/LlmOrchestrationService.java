package com.example.msf_ai.service.impl;

import com.example.msf_ai.client.MsmAiClient;
import com.example.msf_ai.dto.*;
import com.example.msf_ai.service.interfaces.LlmProviderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

@Service
public class LlmOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(LlmOrchestrationService.class);

    private final MsmAiClient msmAiClient;
    private final List<LlmProviderService> providerServices;
    private final OpenAiCompatibleAiServiceImpl openAiCompatibleAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient fastProbeClient;

    public LlmOrchestrationService(MsmAiClient msmAiClient,
                                   List<LlmProviderService> providerServices,
                                   OpenAiCompatibleAiServiceImpl openAiCompatibleAiService) {
        this.msmAiClient = msmAiClient;
        this.providerServices = providerServices;
        this.openAiCompatibleAiService = openAiCompatibleAiService;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(2500));
        requestFactory.setReadTimeout(Duration.ofMillis(3000));
        this.fastProbeClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public List<LlmProviderDto> getSupportedProviders() {
        return List.of(
                new LlmProviderDto(
                        "gemini",
                        "Google Gemini",
                        "gemini-1.5-flash",
                        List.of("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash"),
                        "Model multimodal cepat dan efisien dari Google AI."
                ),
                new LlmProviderDto(
                        "openai",
                        "OpenAI (ChatGPT)",
                        "gpt-4o-mini",
                        List.of("gpt-4o-mini", "gpt-4o", "o1", "o1-mini", "gpt-3.5-turbo"),
                        "Model LLM unggulan dari OpenAI dengan tool calling yang kuat."
                ),
                new LlmProviderDto(
                        "claude",
                        "Anthropic Claude",
                        "claude-3-5-sonnet-20241022",
                        List.of("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-opus-20240229"),
                        "Model penalaran mendalam dan coding presisi dari Anthropic."
                ),
                new LlmProviderDto(
                        "deepseek",
                        "DeepSeek",
                        "deepseek-chat",
                        List.of("deepseek-chat", "deepseek-reasoner"),
                        "Model AI terbuka berbiaya sangat efisien dengan kapabilitas penalaran tinggi."
                ),
                new LlmProviderDto(
                        "mistral",
                        "Mistral AI",
                        "mistral-large-latest",
                        List.of("mistral-large-latest", "mistral-small-latest", "codestral-latest", "open-mistral-nemo"),
                        "Model AI Eropa berkecepatan tinggi dengan tool calling lengkap."
                ),
                new LlmProviderDto(
                        "nvidia",
                        "NVIDIA NIM",
                        "meta/llama-3.1-70b-instruct",
                        List.of("meta/llama-3.1-70b-instruct", "meta/llama-3.1-8b-instruct", "mistralai/mixtral-8x22b-instruct"),
                        "Microservice terakselerasi GPU NVIDIA yang meng-host open-source model mutakhir."
                ),
                new LlmProviderDto(
                        "openrouter",
                        "OpenRouter",
                        "openai/gpt-4o-mini",
                        List.of("openai/gpt-4o-mini", "openai/gpt-4o", "anthropic/claude-3.5-sonnet", "deepseek/deepseek-chat", "google/gemini-2.0-flash-exp:free", "meta-llama/llama-3.3-70b-instruct"),
                        "Gateway AI terpadu untuk mengakses ratusan model LLM terkemuka (Claude, GPT, DeepSeek, Llama, Gemini) via 1 API Key."
                )
        );
    }

    public String detectProviderFromKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return "gemini";
        }
        String key = apiKey.trim();

        if (key.startsWith("AIzaSy") || key.startsWith("AIza")) {
            return "gemini";
        }
        if (key.startsWith("sk-ant-")) {
            return "claude";
        }
        if (key.startsWith("nvapi-")) {
            return "nvidia";
        }
        if (key.startsWith("sk-or-")) {
            return "openrouter";
        }
        if (key.startsWith("sk-proj-")) {
            return "openai";
        }

        // Check if starts with sk- or is generic
        if (key.startsWith("sk-") || key.length() >= 32) {
            String probed = probeCandidates(key);
            if (probed != null) {
                return probed;
            }
            if (key.startsWith("sk-")) {
                return "openai";
            }
        }

        return "gemini";
    }

    private String probeCandidates(String apiKey) {
        // Probe OpenRouter
        try {
            String res = fastProbeClient.get()
                    .uri("https://openrouter.ai/api/v1/models")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header("HTTP-Referer", "http://localhost:8080")
                    .header("X-Title", "MSF AI Assistant")
                    .retrieve()
                    .body(String.class);
            if (res != null && res.contains("data")) {
                return "openrouter";
            }
        } catch (Exception ignored) {}

        // Probe OpenAI
        try {
            String res = fastProbeClient.get()
                    .uri("https://api.openai.com/v1/models")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .body(String.class);
            if (res != null && res.contains("data")) {
                return "openai";
            }
        } catch (Exception ignored) {}

        // Probe DeepSeek
        try {
            String res = fastProbeClient.get()
                    .uri("https://api.deepseek.com/models")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .body(String.class);
            if (res != null && res.contains("data")) {
                return "deepseek";
            }
        } catch (Exception ignored) {}

        // Probe Mistral
        try {
            String res = fastProbeClient.get()
                    .uri("https://api.mistral.ai/v1/models")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .body(String.class);
            if (res != null && res.contains("data")) {
                return "mistral";
            }
        } catch (Exception ignored) {}

        return null;
    }

    public Map<String, Object> detectProviderAndModels(String apiKeyParam, String bearerToken) {
        String apiKey = apiKeyParam != null ? apiKeyParam.trim() : "";
        if (apiKey.isBlank()) {
            apiKey = msmAiClient.getAiConfigValue("active_api_key", "", bearerToken);
            if (apiKey.isBlank()) {
                apiKey = msmAiClient.getAiConfigValue("gemini_api_key", "", bearerToken);
            }
        }

        if (apiKey.isBlank()) {
            List<String> defaultGeminiModels = List.of("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash");
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "API Key tidak boleh kosong.");
            result.put("provider", "gemini");
            result.put("providerName", "Google Gemini");
            result.put("models", defaultGeminiModels);
            result.put("defaultModel", "gemini-1.5-flash");
            return result;
        }

        String provider = detectProviderFromKey(apiKey);
        String providerName = getProviderDisplayName(provider);
        List<String> models = fetchAvailableModels(provider, apiKey, bearerToken);
        String defaultModel = models.isEmpty() ? "gemini-1.5-flash" : models.get(0);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("provider", provider);
        result.put("providerName", providerName);
        result.put("models", models);
        result.put("defaultModel", defaultModel);
        result.put("message", "Berhasil mendeteksi provider: " + providerName + " (" + models.size() + " model tersedia)");
        return result;
    }

    public String getProviderDisplayName(String provider) {
        if (provider == null) return "Google Gemini";
        return switch (provider.trim().toLowerCase()) {
            case "openrouter" -> "OpenRouter";
            case "openai" -> "OpenAI (ChatGPT)";
            case "claude", "anthropic" -> "Anthropic Claude";
            case "deepseek" -> "DeepSeek";
            case "mistral" -> "Mistral AI";
            case "nvidia" -> "NVIDIA NIM";
            default -> "Google Gemini";
        };
    }

    public List<String> fetchAvailableModels(String provider, String apiKey, String bearerToken) {
        String prov = (provider != null && !provider.isBlank()) ? provider.trim().toLowerCase() : detectProviderFromKey(apiKey);
        String resolvedKey = apiKey;

        if (resolvedKey == null || resolvedKey.isBlank()) {
            resolvedKey = msmAiClient.getAiConfigValue(prov + "_api_key", "", bearerToken);
            if (resolvedKey.isBlank() && "gemini".equalsIgnoreCase(prov)) {
                resolvedKey = msmAiClient.getAiConfigValue("gemini_api_key", "", bearerToken);
            }
        }

        if ("deepseek".equals(prov) || "mistral".equals(prov) || "nvidia".equals(prov) || "openai".equals(prov) || "openrouter".equals(prov)) {
            return openAiCompatibleAiService.fetchAvailableModels(resolvedKey, prov);
        }

        for (LlmProviderService svc : providerServices) {
            if (svc.supports(prov)) {
                return svc.fetchAvailableModels(resolvedKey);
            }
        }

        return List.of();
    }

    public LlmTestResponseDto testConnection(String provider, String apiKey, String model, String bearerToken) {
        String resolvedKey = apiKey;
        if (resolvedKey == null || resolvedKey.isBlank()) {
            resolvedKey = msmAiClient.getAiConfigValue("active_api_key", "", bearerToken);
            if (resolvedKey.isBlank()) {
                resolvedKey = msmAiClient.getAiConfigValue("gemini_api_key", "", bearerToken);
            }
        }

        String prov = (provider != null && !provider.isBlank()) ? provider.trim().toLowerCase() : detectProviderFromKey(resolvedKey);
        // Auto-correct if provider defaulted to gemini but key belongs to another provider
        String detected = detectProviderFromKey(resolvedKey);
        if ("gemini".equalsIgnoreCase(prov) && !"gemini".equalsIgnoreCase(detected)) {
            prov = detected;
        }

        log.info("[LLM-ORCHESTRATOR] Testing connection for provider '{}', model '{}'", prov, model);

        if ("deepseek".equals(prov) || "mistral".equals(prov) || "nvidia".equals(prov) || "openai".equals(prov) || "openrouter".equals(prov)) {
            return openAiCompatibleAiService.testConnection(resolvedKey, model, prov);
        }

        for (LlmProviderService svc : providerServices) {
            if (svc.supports(prov)) {
                return svc.testLlmConnection(resolvedKey, model);
            }
        }

        return new LlmTestResponseDto(false, "Provider '" + prov + "' tidak ditemukan", prov, model, List.of());
    }

    public void saveActiveLlmConfig(LlmConfigActiveDto dto, String bearerToken) {
        String apiKey = dto.getApiKey() != null ? dto.getApiKey().trim() : "";
        String prov = dto.getProvider();
        if (prov == null || prov.isBlank()) {
            prov = detectProviderFromKey(apiKey);
        }
        prov = prov.trim().toLowerCase();

        // Auto-correct provider if sent as gemini default but key clearly belongs to another provider
        String detected = detectProviderFromKey(apiKey);
        if ("gemini".equalsIgnoreCase(prov) && !"gemini".equalsIgnoreCase(detected)) {
            prov = detected;
        }

        String model = dto.getModel() != null ? dto.getModel().trim() : "";

        log.info("[LLM-ORCHESTRATOR] Saving active LLM config: provider='{}', model='{}'", prov, model);

        // 1. Simpan active_provider & active_model
        msmAiClient.setAiConfig(new AiConfigDto("active_provider", prov), bearerToken);
        if (!model.isBlank()) {
            msmAiClient.setAiConfig(new AiConfigDto("active_model", model), bearerToken);
            msmAiClient.setAiConfig(new AiConfigDto(prov + "_model", model), bearerToken);
        }

        // 2. Simpan API key
        if (!apiKey.isBlank()) {
            msmAiClient.setAiConfig(new AiConfigDto("active_api_key", apiKey), bearerToken);
            msmAiClient.setAiConfig(new AiConfigDto(prov + "_api_key", apiKey), bearerToken);
        }

        // 3. Backward compatibility untuk gemini
        if ("gemini".equalsIgnoreCase(prov)) {
            if (!apiKey.isBlank()) {
                msmAiClient.setAiConfig(new AiConfigDto("gemini_api_key", apiKey), bearerToken);
            }
            if (!model.isBlank()) {
                msmAiClient.setAiConfig(new AiConfigDto("gemini_model", model), bearerToken);
            }
        }
    }

    public Map<String, String> getActiveLlmConfig(String bearerToken) {
        Map<String, String> allConfigs = msmAiClient.getAllAiConfigs(bearerToken);
        String activeProvider = allConfigs.getOrDefault("active_provider", "gemini");
        String activeModel = allConfigs.getOrDefault("active_model", allConfigs.getOrDefault("gemini_model", "gemini-1.5-flash"));
        String activeKey = allConfigs.getOrDefault(activeProvider + "_api_key", allConfigs.getOrDefault("active_api_key", allConfigs.getOrDefault("gemini_api_key", "")));

        Map<String, String> response = new HashMap<>(allConfigs);
        response.put("active_provider", activeProvider);
        response.put("active_model", activeModel);
        response.put("active_api_key", activeKey);
        response.put("provider_name", getProviderDisplayName(activeProvider));
        return response;
    }

    public ChatResponseDto chat(ChatRequestDto request, String bearerToken) {
        Map<String, String> configs = msmAiClient.getAllAiConfigs(bearerToken);

        String provider = configs.getOrDefault("active_provider", "gemini").trim().toLowerCase();
        String model = configs.getOrDefault("active_model", configs.getOrDefault(provider + "_model", configs.getOrDefault("gemini_model", "gemini-1.5-flash")));
        String apiKey = configs.getOrDefault(provider + "_api_key", configs.getOrDefault("active_api_key", configs.getOrDefault("gemini_api_key", "")));

        List<McpConfigDto> activeMcps = msmAiClient.getActiveMcpConfigs(bearerToken);

        log.info("[LLM-ORCHESTRATOR] Dispatching chat to provider '{}' (model: '{}', activeMcps: {})",
                provider, model, activeMcps.size());

        if ("deepseek".equals(provider) || "mistral".equals(provider) || "nvidia".equals(provider) || "openai".equals(provider) || "openrouter".equals(provider)) {
            return openAiCompatibleAiService.chat(request, apiKey, model, activeMcps, bearerToken, provider);
        }

        for (LlmProviderService svc : providerServices) {
            if (svc.supports(provider)) {
                return svc.chat(request, apiKey, model, activeMcps, bearerToken);
            }
        }

        return new ChatResponseDto("Provider '" + provider + "' belum terintegrasi.", List.of(), false, model);
    }
}
