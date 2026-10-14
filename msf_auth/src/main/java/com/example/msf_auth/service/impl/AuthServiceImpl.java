package com.example.msf_auth.service.impl;

import com.example.library_helper.exception.UnauthorizedException;
import com.example.library_helper.security.JwtTokenService;
import com.example.library_helper.security.RsaKeyProperties;
import com.example.library_helper.security.RsaKeyProvider;
import com.example.msf_auth.client.MsmUserClient;
import com.example.msf_auth.dto.LoginRequestDto;
import com.example.msf_auth.dto.LoginResponseDto;
import com.example.msf_auth.dto.SignUpRequestDto;
import com.example.msf_auth.service.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AuthService in msf_auth.
 * Orchestrates hashing in msf, delegating persistence to msm_user, and issuing RSA-2048 JWT tokens.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss.SSS");

    private final MsmUserClient msmUserClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RsaKeyProvider rsaKeyProvider;
    private final RsaKeyProperties rsaKeyProperties;

    public AuthServiceImpl(MsmUserClient msmUserClient,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           RsaKeyProvider rsaKeyProvider,
                           RsaKeyProperties rsaKeyProperties) {
        this.msmUserClient = msmUserClient;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.rsaKeyProvider = rsaKeyProvider;
        this.rsaKeyProperties = rsaKeyProperties;
    }

    @Override
    public Map<String, Object> signUp(SignUpRequestDto request) {
        log.info("[MSF-SIGNUP] Processing sign-up for name: '{}'", request.getName());

        // Hashing password in msf_auth layer before sending to msm_user
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("[MSF-SIGNUP] Password successfully hashed with BCrypt");

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", request.getName());
        payload.put("loginId", request.getName());
        payload.put("password", hashedPassword);
        payload.put("role", request.getRole() != null ? request.getRole() : "pengguna");
        payload.put("createdBy", "SELF_SIGNUP");
        payload.put("createdDate", LocalDateTime.now().format(DATE_FORMATTER));

        Map<String, Object> createdUser = msmUserClient.createUser(payload);
        log.info("[MSF-SIGNUP] User '{}' successfully created in msm_user", request.getName());

        return createdUser;
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        log.info("[MSF-LOGIN] Processing login attempt for: '{}'", request.getLoginId());

        Map<String, Object> user = msmUserClient.findUser(request.getLoginId());
        String storedHashedPassword = (String) user.get("password");

        if (storedHashedPassword == null || !passwordEncoder.matches(request.getPassword(), storedHashedPassword)) {
            log.warn("[MSF-LOGIN-FAILED] Invalid password for user: '{}'", request.getLoginId());
            throw new UnauthorizedException("Invalid username or password");
        }

        Object active = user.get("active");
        if (Boolean.FALSE.equals(active)) {
            log.warn("[MSF-LOGIN-FAILED] Account is disabled for user: '{}'", request.getLoginId());
            throw new UnauthorizedException("Account is disabled");
        }

        String userId = String.valueOf(user.get("id"));
        String loginId = (String) user.getOrDefault("loginId", user.get("name"));
        String name = (String) user.get("name");
        String role = (String) user.getOrDefault("role", "ROLE_USER");
        List<String> roles = Collections.singletonList(role);

        // Sign token with RSA-2048 private key in msf_auth
        String token = jwtTokenService.generateToken(userId, loginId, roles);
        long expiresIn = rsaKeyProperties.getExpirationMinutes() * 60;

        log.info("[MSF-LOGIN-SUCCESS] Token issued successfully for user '{}', role='{}'", loginId, role);

        return LoginResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresIn(expiresIn)
                .loginId(loginId)
                .name(name)
                .roles(roles)
                .build();
    }

    @Override
    public String getPublicKeyPem() {
        return rsaKeyProvider.getPublicKeyPem();
    }
}
