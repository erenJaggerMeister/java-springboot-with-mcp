package com.example.msf_ai.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.msf_ai.dto.ChatRequestDto;
import com.example.msf_ai.dto.ChatResponseDto;
import com.example.msf_ai.service.impl.LlmOrchestrationService;
import com.example.msf_ai.service.interfaces.GeminiAiService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/msf_ai/chat")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);
    private final LlmOrchestrationService llmOrchestrationService;
    private final GeminiAiService geminiAiService;

    public AiChatController(LlmOrchestrationService llmOrchestrationService,
                            GeminiAiService geminiAiService) {
        this.llmOrchestrationService = llmOrchestrationService;
        this.geminiAiService = geminiAiService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponseDto>> chat(
            @Valid @RequestBody ChatRequestDto request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {

        log.info("REST: POST /api/msf_ai/chat received message length={}", request.getMessage().length());
        ChatResponseDto response = llmOrchestrationService.chat(request, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Pesan berhasil diproses"));
    }
}
