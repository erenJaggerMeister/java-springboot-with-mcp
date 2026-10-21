package com.example.msf_ai.service.interfaces;

import com.example.msf_ai.dto.ChatRequestDto;
import com.example.msf_ai.dto.ChatResponseDto;
import com.example.msf_ai.dto.GeminiTestResponseDto;

import java.util.List;

public interface GeminiAiService {
    ChatResponseDto chat(ChatRequestDto request, String bearerToken);
    boolean testApiKey(String apiKey);
    GeminiTestResponseDto testConnection(String apiKey, String model);
    List<String> fetchAvailableModels(String apiKey);
}
