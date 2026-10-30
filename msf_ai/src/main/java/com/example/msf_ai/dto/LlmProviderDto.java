package com.example.msf_ai.dto;

import java.io.Serializable;
import java.util.List;

public class LlmProviderDto implements Serializable {
    private String id;
    private String name;
    private String defaultModel;
    private List<String> defaultModels;
    private String description;

    public LlmProviderDto() {}

    public LlmProviderDto(String id, String name, String defaultModel, List<String> defaultModels, String description) {
        this.id = id;
        this.name = name;
        this.defaultModel = defaultModel;
        this.defaultModels = defaultModels;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public List<String> getDefaultModels() {
        return defaultModels;
    }

    public void setDefaultModels(List<String> defaultModels) {
        this.defaultModels = defaultModels;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
