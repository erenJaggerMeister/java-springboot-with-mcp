package com.example.msm_ai.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.msm_ai.dto.AiConfigDto;
import com.example.msm_ai.service.interfaces.AiConfigService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/msm_ai/config")
public class AiConfigController {

    private static final Logger log = LoggerFactory.getLogger(AiConfigController.class);
    private final AiConfigService aiConfigService;

    public AiConfigController(AiConfigService aiConfigService) {
        this.aiConfigService = aiConfigService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> getAllConfigs() {
        log.info("REST: GET all AI configs");
        Map<String, String> configs = aiConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs, "AI configurations retrieved successfully"));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<String>> getConfigValue(@PathVariable("key") String key,
                                                              @RequestParam(name = "default", required = false) String defaultValue) {
        log.info("REST: GET AI config for key: '{}'", key);
        String value = aiConfigService.getConfigValue(key, defaultValue != null ? defaultValue : "");
        return ResponseEntity.ok(ApiResponse.success(value, "AI config value retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AiConfigDto>> setConfig(@Valid @RequestBody AiConfigDto dto) {
        log.info("REST: SET AI config key: '{}'", dto.getConfigKey());
        AiConfigDto saved = aiConfigService.setConfig(dto);
        return ResponseEntity.ok(ApiResponse.success(saved, "AI config saved successfully"));
    }
}
