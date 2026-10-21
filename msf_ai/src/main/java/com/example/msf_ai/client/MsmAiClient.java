package com.example.msf_ai.client;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.exception.BadRequestException;
import com.example.msf_ai.dto.AiConfigDto;
import com.example.msf_ai.dto.McpConfigCreateDto;
import com.example.msf_ai.dto.McpConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class MsmAiClient {

    private static final Logger log = LoggerFactory.getLogger(MsmAiClient.class);
    private final RestClient restClient;

    public MsmAiClient(@Value("${msm.ai-service.url:http://localhost:8085}") String msmAiUrl) {
        log.info("Configuring MsmAiClient pointing to: {}", msmAiUrl);
        this.restClient = RestClient.builder()
                .baseUrl(msmAiUrl)
                .build();
    }

    public Map<String, String> getAllAiConfigs(String bearerToken) {
        try {
            ApiResponse<Map<String, String>> response = restClient.get()
                    .uri("/api/msm_ai/config")
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<Map<String, String>>>() {});
            return response != null && response.getData() != null ? response.getData() : Map.of();
        } catch (Exception e) {
            log.warn("Failed to get all AI configs from msm_ai: {}", e.getMessage());
            return Map.of();
        }
    }

    public String getAiConfigValue(String key, String defaultValue, String bearerToken) {
        try {
            ApiResponse<String> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/msm_ai/config/{key}")
                            .queryParam("default", defaultValue)
                            .build(key))
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<String>>() {});
            return response != null && response.getData() != null ? response.getData() : defaultValue;
        } catch (Exception e) {
            log.warn("Failed to get AI config '{}' from msm_ai: {}", key, e.getMessage());
            return defaultValue;
        }
    }

    public AiConfigDto setAiConfig(AiConfigDto dto, String bearerToken) {
        try {
            ApiResponse<AiConfigDto> response = restClient.post()
                    .uri("/api/msm_ai/config")
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<AiConfigDto>>() {});
            return response != null ? response.getData() : dto;
        } catch (Exception e) {
            log.error("Failed to set AI config in msm_ai: {}", e.getMessage());
            throw new BadRequestException("Gagal menyimpan konfigurasi AI: " + e.getMessage());
        }
    }

    public List<McpConfigDto> getAllMcpConfigs(String bearerToken) {
        try {
            ApiResponse<List<McpConfigDto>> response = restClient.get()
                    .uri("/api/msm_ai/mcp")
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<McpConfigDto>>>() {});
            return response != null && response.getData() != null ? response.getData() : List.of();
        } catch (Exception e) {
            log.warn("Failed to get all MCP configs from msm_ai: {}", e.getMessage());
            return List.of();
        }
    }

    public List<McpConfigDto> getActiveMcpConfigs(String bearerToken) {
        try {
            ApiResponse<List<McpConfigDto>> response = restClient.get()
                    .uri("/api/msm_ai/mcp/active")
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<List<McpConfigDto>>>() {});
            return response != null && response.getData() != null ? response.getData() : List.of();
        } catch (Exception e) {
            log.warn("Failed to get active MCP configs from msm_ai: {}", e.getMessage());
            return List.of();
        }
    }

    public McpConfigDto createMcpConfig(McpConfigCreateDto dto, String bearerToken) {
        try {
            ApiResponse<McpConfigDto> response = restClient.post()
                    .uri("/api/msm_ai/mcp")
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<McpConfigDto>>() {});
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Failed to create MCP config in msm_ai: {}", e.getMessage());
            throw new BadRequestException("Gagal menambah konfigurasi MCP: " + e.getMessage());
        }
    }

    public McpConfigDto updateMcpConfig(UUID id, McpConfigCreateDto dto, String bearerToken) {
        try {
            ApiResponse<McpConfigDto> response = restClient.put()
                    .uri("/api/msm_ai/mcp/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<McpConfigDto>>() {});
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Failed to update MCP config in msm_ai: {}", e.getMessage());
            throw new BadRequestException("Gagal mengupdate konfigurasi MCP: " + e.getMessage());
        }
    }

    public McpConfigDto toggleMcpStatus(UUID id, Boolean active, String bearerToken) {
        try {
            ApiResponse<McpConfigDto> response = restClient.patch()
                    .uri(uriBuilder -> uriBuilder.path("/api/msm_ai/mcp/{id}/toggle")
                            .queryParam("active", active)
                            .build(id))
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<McpConfigDto>>() {});
            return response != null ? response.getData() : null;
        } catch (Exception e) {
            log.error("Failed to toggle MCP config status in msm_ai: {}", e.getMessage());
            throw new BadRequestException("Gagal mengubah status aktif MCP: " + e.getMessage());
        }
    }

    public void deleteMcpConfig(UUID id, String bearerToken) {
        try {
            restClient.delete()
                    .uri("/api/msm_ai/mcp/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, normalizeBearer(bearerToken))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Failed to delete MCP config in msm_ai: {}", e.getMessage());
            throw new BadRequestException("Gagal menghapus konfigurasi MCP: " + e.getMessage());
        }
    }

    private String normalizeBearer(String token) {
        if (token == null || token.isBlank()) return "";
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }
}
