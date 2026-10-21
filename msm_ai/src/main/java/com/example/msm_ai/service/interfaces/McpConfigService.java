package com.example.msm_ai.service.interfaces;

import com.example.msm_ai.dto.McpConfigCreateDto;
import com.example.msm_ai.dto.McpConfigDto;
import java.util.List;
import java.util.UUID;

public interface McpConfigService {
    List<McpConfigDto> getAllMcpConfigs();
    List<McpConfigDto> getActiveMcpConfigs();
    McpConfigDto createMcpConfig(McpConfigCreateDto dto);
    McpConfigDto updateMcpConfig(UUID id, McpConfigCreateDto dto);
    McpConfigDto toggleMcpStatus(UUID id, Boolean isActive);
    void deleteMcpConfig(UUID id);
}
