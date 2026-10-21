package com.example.msf_ai.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.msf_ai.dto.McpTestRequestDto;
import com.example.msf_ai.dto.McpTestResponseDto;
import com.example.msf_ai.service.interfaces.McpClientService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/msf_ai/mcp")
public class McpTestController {

    private static final Logger log = LoggerFactory.getLogger(McpTestController.class);
    private final McpClientService mcpClientService;

    public McpTestController(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @PostMapping("/test")
    public ResponseEntity<ApiResponse<McpTestResponseDto>> testMcpConnection(
            @Valid @RequestBody McpTestRequestDto request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {

        log.info("REST: POST /api/msf_ai/mcp/test to URL '{}'", request.getUrl());
        McpTestResponseDto result = mcpClientService.testConnection(request.getUrl(), bearerToken);
        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }
}
