package com.example.msm_ai.service.impl;

import com.example.library_helper.exception.ResourceNotFoundException;
import com.example.msm_ai.dto.McpConfigCreateDto;
import com.example.msm_ai.dto.McpConfigDto;
import com.example.msm_ai.model.McpConfig;
import com.example.msm_ai.repository.McpConfigRepository;
import com.example.msm_ai.service.interfaces.McpConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class McpConfigServiceImpl implements McpConfigService {

    private static final Logger log = LoggerFactory.getLogger(McpConfigServiceImpl.class);
    private final McpConfigRepository mcpConfigRepository;

    public McpConfigServiceImpl(McpConfigRepository mcpConfigRepository) {
        this.mcpConfigRepository = mcpConfigRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<McpConfigDto> getAllMcpConfigs() {
        return mcpConfigRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<McpConfigDto> getActiveMcpConfigs() {
        return mcpConfigRepository.findByIsActiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public McpConfigDto createMcpConfig(McpConfigCreateDto dto) {
        log.info("Creating new MCP config: name='{}', url='{}'", dto.getName(), dto.getUrl());
        McpConfig entity = new McpConfig(dto.getName(), dto.getUrl(), dto.getIsActive());
        McpConfig saved = mcpConfigRepository.save(entity);
        return mapToDto(saved);
    }

    @Override
    public McpConfigDto updateMcpConfig(UUID id, McpConfigCreateDto dto) {
        log.info("Updating MCP config id={}: name='{}', url='{}'", id, dto.getName(), dto.getUrl());
        McpConfig entity = mcpConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCP config not found with id: " + id));

        entity.setName(dto.getName());
        entity.setUrl(dto.getUrl());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        McpConfig saved = mcpConfigRepository.save(entity);
        return mapToDto(saved);
    }

    @Override
    public McpConfigDto toggleMcpStatus(UUID id, Boolean isActive) {
        log.info("Toggling MCP status id={} to isActive={}", id, isActive);
        McpConfig entity = mcpConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCP config not found with id: " + id));

        entity.setIsActive(isActive);
        entity.setUpdatedAt(LocalDateTime.now());
        McpConfig saved = mcpConfigRepository.save(entity);
        return mapToDto(saved);
    }

    @Override
    public void deleteMcpConfig(UUID id) {
        log.info("Deleting MCP config id={}", id);
        if (!mcpConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("MCP config not found with id: " + id);
        }
        mcpConfigRepository.deleteById(id);
    }

    private McpConfigDto mapToDto(McpConfig entity) {
        return new McpConfigDto(
                entity.getId(),
                entity.getName(),
                entity.getUrl(),
                entity.getIsActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
