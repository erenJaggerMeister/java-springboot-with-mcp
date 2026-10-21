package com.example.msf_ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatRequestDto implements Serializable {

    @NotBlank(message = "Pesan user tidak boleh kosong")
    private String message;

    private List<ChatMessageDto> conversationHistory = new ArrayList<>();

    public ChatRequestDto() {}

    public ChatRequestDto(String message, List<ChatMessageDto> conversationHistory) {
        this.message = message;
        this.conversationHistory = conversationHistory != null ? conversationHistory : new ArrayList<>();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageDto> getConversationHistory() {
        return conversationHistory;
    }

    public void setConversationHistory(List<ChatMessageDto> conversationHistory) {
        this.conversationHistory = conversationHistory;
    }
}
