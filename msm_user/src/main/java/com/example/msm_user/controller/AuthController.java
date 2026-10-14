package com.example.msm_user.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.msm_user.dto.LoginRequestDto;
import com.example.msm_user.dto.LoginResponseDto;
import com.example.msm_user.service.interfaces.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing authentication and public key endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("REST request to authenticate user: {}", request.getLoginId());
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @GetMapping(value = "/public-key", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getPublicKey() {
        log.info("REST request to fetch RSA-2048 public key");
        String publicKeyPem = authService.getPublicKeyPem();
        return ResponseEntity.ok(publicKeyPem);
    }
}
