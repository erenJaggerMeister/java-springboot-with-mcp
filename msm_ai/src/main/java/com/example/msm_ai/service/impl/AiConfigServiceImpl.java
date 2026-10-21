package com.example.msm_ai.service.impl;

import com.example.msm_ai.dto.AiConfigDto;
import com.example.msm_ai.model.AiConfig;
import com.example.msm_ai.repository.AiConfigRepository;
import com.example.msm_ai.service.interfaces.AiConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class AiConfigServiceImpl implements AiConfigService {

    private static final Logger log = LoggerFactory.getLogger(AiConfigServiceImpl.class);
    private final AiConfigRepository aiConfigRepository;

    public AiConfigServiceImpl(AiConfigRepository aiConfigRepository) {
        this.aiConfigRepository = aiConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getAllConfigs() {
        Map<String, String> configs = new HashMap<>();
        for (AiConfig entity : aiConfigRepository.findAll()) {
            configs.put(entity.getConfigKey(), entity.getConfigValue());
        }
        return configs;
    }

    @Override
    @Transactional(readOnly = true)
    public String getConfigValue(String key, String defaultValue) {
        return aiConfigRepository.findByConfigKey(key)
                .map(AiConfig::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    public AiConfigDto setConfig(AiConfigDto dto) {
        log.info("Setting AI config key: '{}'", dto.getConfigKey());
        AiConfig config = aiConfigRepository.findByConfigKey(dto.getConfigKey())
                .orElseGet(() -> new AiConfig(dto.getConfigKey(), dto.getConfigValue()));

        config.setConfigValue(dto.getConfigValue());
        config.setUpdatedAt(LocalDateTime.now());
        AiConfig saved = aiConfigRepository.save(config);

        return new AiConfigDto(saved.getConfigKey(), saved.getConfigValue());
    }
}
