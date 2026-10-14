package com.example.msm_user.service.impl;

import com.example.library_helper.exception.UnauthorizedException;
import com.example.library_helper.security.JwtTokenService;
import com.example.library_helper.security.RsaKeyProperties;
import com.example.library_helper.security.RsaKeyProvider;
import com.example.msm_user.dto.LoginRequestDto;
import com.example.msm_user.dto.LoginResponseDto;
import com.example.msm_user.model.User;
import com.example.msm_user.repository.UserRepository;
import com.example.msm_user.service.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of AuthService for authenticating users and issuing RSA-2048 Bearer tokens.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final RsaKeyProvider rsaKeyProvider;
    private final RsaKeyProperties rsaKeyProperties;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenService jwtTokenService,
                           RsaKeyProvider rsaKeyProvider,
                           RsaKeyProperties rsaKeyProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.rsaKeyProvider = rsaKeyProvider;
        this.rsaKeyProperties = rsaKeyProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request) {
        log.info("Processing authentication attempt for login_id: {}", request.getLoginId());

        User user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User with login_id '{}' not found", request.getLoginId());
                    return new UnauthorizedException("Invalid login credentials");
                });

        if (Boolean.FALSE.equals(user.getActive())) {
            log.warn("Authentication failed: User account '{}' is deactivated", request.getLoginId());
            throw new UnauthorizedException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Authentication failed: Invalid password for login_id '{}'", request.getLoginId());
            throw new UnauthorizedException("Invalid login credentials");
        }

        List<String> roles = Collections.singletonList(user.getRole());
        String token = jwtTokenService.generateToken(user.getId().toString(), user.getLoginId(), roles);
        long expiresIn = rsaKeyProperties.getExpirationMinutes() * 60;

        log.info("Authentication successful for login_id: {}. Role: {}, Expires in: {}s",
                user.getLoginId(), user.getRole(), expiresIn);

        return LoginResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(token)
                .expiresIn(expiresIn)
                .loginId(user.getLoginId())
                .name(user.getName())
                .roles(roles)
                .build();
    }

    @Override
    public String getPublicKeyPem() {
        log.info("Fetching RSA-2048 Public Key in PEM format");
        return rsaKeyProvider.getPublicKeyPem();
    }
}
