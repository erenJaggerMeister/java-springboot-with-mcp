package com.example.msm_ai.service.interfaces;

import com.example.msm_ai.dto.AiConfigDto;
import java.util.Map;

public interface AiConfigService {
    Map<String, String> getAllConfigs();
    String getConfigValue(String key, String defaultValue);
    AiConfigDto setConfig(AiConfigDto dto);
}
