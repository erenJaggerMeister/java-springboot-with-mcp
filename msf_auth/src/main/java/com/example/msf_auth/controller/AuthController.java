package com.example.msf_auth.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.msf_auth.dto.LoginRequestDto;
import com.example.msf_auth.dto.LoginResponseDto;
import com.example.msf_auth.dto.SignUpRequestDto;
import com.example.msf_auth.service.interfaces.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller exposing authentication, registration, and public key endpoints in msf_auth.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Map<String, Object>>> signUp(@Valid @RequestBody SignUpRequestDto request) {
        log.info("REST request to sign-up user: name='{}'", request.getName());
        Map<String, Object> created = authService.signUp(request);
        return new ResponseEntity<>(ApiResponse.success(created, "User registered successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("REST request to login user: '{}'", request.getLoginId());
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @GetMapping(value = "/public-key", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getPublicKey() {
        log.info("REST request to retrieve RSA-2048 public key");
        return ResponseEntity.ok(authService.getPublicKeyPem());
    }
}
