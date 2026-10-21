package com.example.msf_ai.service.interfaces;

import com.example.msf_ai.dto.McpTestResponseDto;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

public interface McpClientService {
    McpTestResponseDto testConnection(String mcpUrl, String bearerToken);
    List<JsonNode> fetchAvailableTools(String mcpUrl, String bearerToken);
    JsonNode executeToolCall(String mcpUrl, String toolName, Map<String, Object> arguments, String bearerToken);
}
