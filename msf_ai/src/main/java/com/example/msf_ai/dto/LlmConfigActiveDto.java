package com.example.msf_ai.dto;

import java.io.Serializable;

public class LlmConfigActiveDto implements Serializable {
    private String provider;
    private String apiKey;
    private String model;

    public LlmConfigActiveDto() {}

    public LlmConfigActiveDto(String provider, String apiKey, String model) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
