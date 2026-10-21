package com.example.msf_ai.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GeminiTestResponseDto implements Serializable {
    private boolean success;
    private String message;
    private String modelTested;
    private List<String> availableModels = new ArrayList<>();

    public GeminiTestResponseDto() {}

    public GeminiTestResponseDto(boolean success, String message, String modelTested, List<String> availableModels) {
        this.success = success;
        this.message = message;
        this.modelTested = modelTested;
        this.availableModels = availableModels != null ? availableModels : new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModelTested() {
        return modelTested;
    }

    public void setModelTested(String modelTested) {
        this.modelTested = modelTested;
    }

    public List<String> getAvailableModels() {
        return availableModels;
    }

    public void setAvailableModels(List<String> availableModels) {
        this.availableModels = availableModels;
    }
}
