package com.example.msm_ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

public class McpConfigCreateDto implements Serializable {
    @NotBlank(message = "Nama MCP tidak boleh kosong")
    private String name;

    @NotBlank(message = "URL MCP tidak boleh kosong")
    private String url;

    private Boolean isActive = true;

    public McpConfigCreateDto() {}

    public McpConfigCreateDto(String name, String url, Boolean isActive) {
        this.name = name;
        this.url = url;
        this.isActive = isActive != null ? isActive : true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
