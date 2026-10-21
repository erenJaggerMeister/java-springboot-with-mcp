package com.example.msf_ai.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class McpTestResponseDto implements Serializable {
    private boolean connected;
    private String message;
    private int toolsCount;
    private List<String> tools = new ArrayList<>();

    public McpTestResponseDto() {}

    public McpTestResponseDto(boolean connected, String message, int toolsCount, List<String> tools) {
        this.connected = connected;
        this.message = message;
        this.toolsCount = toolsCount;
        this.tools = tools != null ? tools : new ArrayList<>();
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getToolsCount() {
        return toolsCount;
    }

    public void setToolsCount(int toolsCount) {
        this.toolsCount = toolsCount;
    }

    public List<String> getTools() {
        return tools;
    }

    public void setTools(List<String> tools) {
        this.tools = tools;
    }
}
