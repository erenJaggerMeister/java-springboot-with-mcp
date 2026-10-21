package com.example.msm_ai.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.msm_ai.dto.McpConfigCreateDto;
import com.example.msm_ai.dto.McpConfigDto;
import com.example.msm_ai.service.interfaces.McpConfigService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/msm_ai/mcp")
public class McpConfigController {

    private static final Logger log = LoggerFactory.getLogger(McpConfigController.class);
    private final McpConfigService mcpConfigService;

    public McpConfigController(McpConfigService mcpConfigService) {
        this.mcpConfigService = mcpConfigService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<McpConfigDto>>> getAllMcpConfigs() {
        log.info("REST: GET all MCP configs");
        List<McpConfigDto> list = mcpConfigService.getAllMcpConfigs();
        return ResponseEntity.ok(ApiResponse.success(list, "MCP configs retrieved successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<McpConfigDto>>> getActiveMcpConfigs() {
        log.info("REST: GET active MCP configs");
        List<McpConfigDto> list = mcpConfigService.getActiveMcpConfigs();
        return ResponseEntity.ok(ApiResponse.success(list, "Active MCP configs retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<McpConfigDto>> createMcpConfig(@Valid @RequestBody McpConfigCreateDto dto) {
        log.info("REST: CREATE MCP config: {}", dto.getName());
        McpConfigDto created = mcpConfigService.createMcpConfig(dto);
        return new ResponseEntity<>(ApiResponse.success(created, "MCP config created successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<McpConfigDto>> updateMcpConfig(@PathVariable("id") UUID id,
                                                                     @Valid @RequestBody McpConfigCreateDto dto) {
        log.info("REST: UPDATE MCP config id={}", id);
        McpConfigDto updated = mcpConfigService.updateMcpConfig(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "MCP config updated successfully"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<McpConfigDto>> toggleMcpStatus(@PathVariable("id") UUID id,
                                                                     @RequestParam("active") Boolean active) {
        log.info("REST: TOGGLE MCP config id={} active={}", id, active);
        McpConfigDto updated = mcpConfigService.toggleMcpStatus(id, active);
        return ResponseEntity.ok(ApiResponse.success(updated, "MCP status toggled successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMcpConfig(@PathVariable("id") UUID id) {
        log.info("REST: DELETE MCP config id={}", id);
        mcpConfigService.deleteMcpConfig(id);
        return ResponseEntity.ok(ApiResponse.success(null, "MCP config deleted successfully"));
    }
}
