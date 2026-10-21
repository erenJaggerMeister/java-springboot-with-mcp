package com.example.msf_ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class AiConfigDto implements Serializable {
    @NotBlank(message = "Config key cannot be blank")
    private String configKey;

    @NotBlank(message = "Config value cannot be blank")
    private String configValue;

    public AiConfigDto() {}

    public AiConfigDto(String configKey, String configValue) {
        this.configKey = configKey;
        this.configValue = configValue;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }
}
