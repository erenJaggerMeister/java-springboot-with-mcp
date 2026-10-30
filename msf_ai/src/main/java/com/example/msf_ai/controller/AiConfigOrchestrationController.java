package com.example.msf_ai.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.msf_ai.client.MsmAiClient;
import com.example.msf_ai.dto.*;
import com.example.msf_ai.service.impl.LlmOrchestrationService;
import com.example.msf_ai.service.interfaces.GeminiAiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/msf_ai/config")
public class AiConfigOrchestrationController {

    private static final Logger log = LoggerFactory.getLogger(AiConfigOrchestrationController.class);
    private final MsmAiClient msmAiClient;
    private final GeminiAiService geminiAiService;
    private final LlmOrchestrationService llmOrchestrationService;

    public AiConfigOrchestrationController(MsmAiClient msmAiClient,
                                          GeminiAiService geminiAiService,
                                          LlmOrchestrationService llmOrchestrationService) {
        this.msmAiClient = msmAiClient;
        this.geminiAiService = geminiAiService;
        this.llmOrchestrationService = llmOrchestrationService;
    }

    // --- MULTI-PROVIDER LLM CONFIGURATION ENDPOINTS ---

    @PostMapping("/detect")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detectProviderAndModels(
            @RequestBody(required = false) Map<String, String> payload,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        String apiKey = payload != null ? payload.get("apiKey") : null;
        log.info("REST: POST /api/msf_ai/config/detect");
        Map<String, Object> result = llmOrchestrationService.detectProviderAndModels(apiKey, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }

    @GetMapping("/detect")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detectProviderAndModelsGet(
            @RequestParam(name = "apiKey", required = false) String apiKey,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: GET /api/msf_ai/config/detect");
        Map<String, Object> result = llmOrchestrationService.detectProviderAndModels(apiKey, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(result, (String) result.get("message")));
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<LlmProviderDto>>> getSupportedProviders() {
        log.info("REST: GET /api/msf_ai/config/providers");
        List<LlmProviderDto> providers = llmOrchestrationService.getSupportedProviders();
        return ResponseEntity.ok(ApiResponse.success(providers, "Daftar provider LLM yang didukung"));
    }

    @GetMapping("/models")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableModels(
            @RequestParam(name = "provider", defaultValue = "gemini") String provider,
            @RequestParam(name = "apiKey", required = false) String apiKey,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: GET /api/msf_ai/config/models provider={}", provider);
        List<String> models = llmOrchestrationService.fetchAvailableModels(provider, apiKey, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(models, "Daftar model berhasil diambil"));
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<LlmTestResponseDto>> testLlmConnection(
            @RequestBody LlmTestRequestDto request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: POST /api/msf_ai/config/test provider={}, model={}", request.getProvider(), request.getModel());
        LlmTestResponseDto result = llmOrchestrationService.testConnection(
                request.getProvider(),
                request.getApiKey(),
                request.getModel(),
                bearerToken
        );
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @PostMapping("/active")
    public ResponseEntity<ApiResponse<String>> saveActiveLlmConfig(
            @RequestBody LlmConfigActiveDto dto,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: POST /api/msf_ai/config/active provider={}, model={}", dto.getProvider(), dto.getModel());
        llmOrchestrationService.saveActiveLlmConfig(dto, bearerToken);
        return ResponseEntity.ok(ApiResponse.success("Konfigurasi LLM aktif berhasil disimpan", "Sukses"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Map<String, String>>> getActiveLlmConfig(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: GET /api/msf_ai/config/active");
        Map<String, String> config = llmOrchestrationService.getActiveLlmConfig(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(config, "Konfigurasi LLM aktif berhasil diambil"));
    }

    // --- GEMINI CONFIGURATION ENDPOINTS (PRESERVED FOR BACKWARD COMPATIBILITY) ---

    @GetMapping("/gemini")
    public ResponseEntity<ApiResponse<Map<String, String>>> getGeminiConfig(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: GET /api/msf_ai/config/gemini");
        Map<String, String> configs = msmAiClient.getAllAiConfigs(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(configs, "Konfigurasi AI berhasil diambil"));
    }

    @PostMapping("/gemini")
    public ResponseEntity<ApiResponse<AiConfigDto>> saveGeminiConfig(
            @Valid @RequestBody AiConfigDto dto,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: POST /api/msf_ai/config/gemini key={}", dto.getConfigKey());
        AiConfigDto saved = msmAiClient.setAiConfig(dto, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(saved, "Konfigurasi AI berhasil disimpan"));
    }

    @PostMapping("/gemini/test")
    public ResponseEntity<ApiResponse<GeminiTestResponseDto>> testGeminiApiKey(@RequestBody Map<String, String> payload) {
        String apiKey = payload.get("apiKey");
        String model = payload.get("model");
        log.info("REST: POST /api/msf_ai/config/gemini/test (model: {})", model);
        GeminiTestResponseDto result = geminiAiService.testConnection(apiKey, model);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    @GetMapping("/gemini/models")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableGeminiModels(
            @RequestParam(name = "apiKey", required = false) String apiKeyParam,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: GET /api/msf_ai/config/gemini/models");
        String apiKey = apiKeyParam;
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = msmAiClient.getAiConfigValue("gemini_api_key", "", bearerToken);
        }
        List<String> models = geminiAiService.fetchAvailableModels(apiKey);
        return ResponseEntity.ok(ApiResponse.success(models, "Daftar model Gemini berhasil diambil"));
    }

    // --- MCP SERVER CONFIGURATION ENDPOINTS (PLUG-IN / PLUG-OUT) ---

    @GetMapping("/mcp")
    public ResponseEntity<ApiResponse<List<McpConfigDto>>> getAllMcpConfigs(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: GET /api/msf_ai/config/mcp");
        List<McpConfigDto> list = msmAiClient.getAllMcpConfigs(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(list, "Daftar konfigurasi MCP berhasil diambil"));
    }

    @GetMapping("/mcp/active")
    public ResponseEntity<ApiResponse<List<McpConfigDto>>> getActiveMcpConfigs(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: GET /api/msf_ai/config/mcp/active");
        List<McpConfigDto> list = msmAiClient.getActiveMcpConfigs(bearerToken);
        return ResponseEntity.ok(ApiResponse.success(list, "Daftar MCP aktif berhasil diambil"));
    }

    @PostMapping("/mcp")
    public ResponseEntity<ApiResponse<McpConfigDto>> createMcpConfig(
            @Valid @RequestBody McpConfigCreateDto dto,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: POST /api/msf_ai/config/mcp name={}", dto.getName());
        McpConfigDto created = msmAiClient.createMcpConfig(dto, bearerToken);
        return new ResponseEntity<>(ApiResponse.success(created, "Konfigurasi MCP berhasil ditambahkan"), HttpStatus.CREATED);
    }

    @PutMapping("/mcp/{id}")
    public ResponseEntity<ApiResponse<McpConfigDto>> updateMcpConfig(
            @PathVariable("id") UUID id,
            @Valid @RequestBody McpConfigCreateDto dto,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: PUT /api/msf_ai/config/mcp/{}", id);
        McpConfigDto updated = msmAiClient.updateMcpConfig(id, dto, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(updated, "Konfigurasi MCP berhasil diperbarui"));
    }

    @PatchMapping("/mcp/{id}/toggle")
    public ResponseEntity<ApiResponse<McpConfigDto>> toggleMcpStatus(
            @PathVariable("id") UUID id,
            @RequestParam("active") Boolean active,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: PATCH /api/msf_ai/config/mcp/{}/toggle active={}", id, active);
        McpConfigDto updated = msmAiClient.toggleMcpStatus(id, active, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(updated, "Status plug-in/plug-out MCP berhasil diubah"));
    }

    @DeleteMapping("/mcp/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMcpConfig(
            @PathVariable("id") UUID id,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST: DELETE /api/msf_ai/config/mcp/{}", id);
        msmAiClient.deleteMcpConfig(id, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Konfigurasi MCP berhasil dihapus"));
    }
}
