package com.example.msf_ai.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatResponseDto implements Serializable {

    private String reply;
    private List<String> toolsUsed = new ArrayList<>();
    private boolean toolExecuted;
    private String model;

    public ChatResponseDto() {}

    public ChatResponseDto(String reply, List<String> toolsUsed, boolean toolExecuted, String model) {
        this.reply = reply;
        this.toolsUsed = toolsUsed != null ? toolsUsed : new ArrayList<>();
        this.toolExecuted = toolExecuted;
        this.model = model;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getToolsUsed() {
        return toolsUsed;
    }

    public void setToolsUsed(List<String> toolsUsed) {
        this.toolsUsed = toolsUsed;
    }

    public boolean isToolExecuted() {
        return toolExecuted;
    }

    public void setToolExecuted(boolean toolExecuted) {
        this.toolExecuted = toolExecuted;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
