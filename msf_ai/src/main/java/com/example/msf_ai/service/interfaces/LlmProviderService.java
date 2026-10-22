package com.example.msf_ai.service.interfaces;

import com.example.msf_ai.dto.ChatRequestDto;
import com.example.msf_ai.dto.ChatResponseDto;
import com.example.msf_ai.dto.LlmTestResponseDto;
import com.example.msf_ai.dto.McpConfigDto;

import java.util.List;

public interface LlmProviderService {
    boolean supports(String provider);
    List<String> fetchAvailableModels(String apiKey);
    LlmTestResponseDto testLlmConnection(String apiKey, String model);
    ChatResponseDto chat(ChatRequestDto request, String apiKey, String model, List<McpConfigDto> activeMcps, String bearerToken);
}
