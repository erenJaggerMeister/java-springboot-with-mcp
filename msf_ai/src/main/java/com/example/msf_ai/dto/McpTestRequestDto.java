package com.example.msf_ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class McpTestRequestDto implements Serializable {
    @NotBlank(message = "URL MCP tidak boleh kosong")
    private String url;

    public McpTestRequestDto() {}

    public McpTestRequestDto(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
