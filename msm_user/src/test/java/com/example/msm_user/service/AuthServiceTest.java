package com.example.msm_user.service;

import com.example.library_helper.exception.UnauthorizedException;
import com.example.library_helper.security.JwtTokenService;
import com.example.library_helper.security.RsaKeyProperties;
import com.example.library_helper.security.RsaKeyProvider;
import com.example.msm_user.dto.LoginRequestDto;
import com.example.msm_user.dto.LoginResponseDto;
import com.example.msm_user.model.User;
import com.example.msm_user.repository.UserRepository;
import com.example.msm_user.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private RsaKeyProvider rsaKeyProvider;
    private RsaKeyProperties rsaKeyProperties;
    private JwtTokenService jwtTokenService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        rsaKeyProperties = new RsaKeyProperties();
        rsaKeyProperties.setExpirationMinutes(60);
        rsaKeyProperties.setKeyId("test-rsa-key");
        rsaKeyProperties.setIssuer("test-issuer");

        rsaKeyProvider = new RsaKeyProvider(rsaKeyProperties);
        rsaKeyProvider.init();

        jwtTokenService = new JwtTokenService(rsaKeyProvider, rsaKeyProperties);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtTokenService, rsaKeyProvider, rsaKeyProperties);
    }

    @Test
    void testLoginSuccess() {
        User user = new User(UUID.randomUUID(), "marcellius@example.com", "hashed_pwd", "Marcellius",
                "ROLE_ADMIN", true, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findByLoginId("marcellius@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed_pwd")).thenReturn(true);

        LoginRequestDto request = new LoginRequestDto("marcellius@example.com", "Password123!");
        LoginResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getAccessToken());
        assertFalse(response.getAccessToken().isBlank());
        assertEquals("marcellius@example.com", response.getLoginId());
        assertEquals("Marcellius", response.getName());
        assertEquals(List.of("ROLE_ADMIN"), response.getRoles());

        // Also verify the token is validly signed with RSA-2048
        assertEquals("marcellius@example.com", jwtTokenService.getLoginId(response.getAccessToken()));
        assertEquals(List.of("ROLE_ADMIN"), jwtTokenService.getRoles(response.getAccessToken()));
    }

    @Test
    void testLoginInvalidPasswordThrowsUnauthorized() {
        User user = new User(UUID.randomUUID(), "marcellius@example.com", "hashed_pwd", "Marcellius",
                "ROLE_ADMIN", true, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findByLoginId("marcellius@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed_pwd")).thenReturn(false);

        LoginRequestDto request = new LoginRequestDto("marcellius@example.com", "WrongPassword");
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLoginDisabledUserThrowsUnauthorized() {
        User user = new User(UUID.randomUUID(), "marcellius@example.com", "hashed_pwd", "Marcellius",
                "ROLE_ADMIN", false, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findByLoginId("marcellius@example.com")).thenReturn(Optional.of(user));

        LoginRequestDto request = new LoginRequestDto("marcellius@example.com", "Password123!");
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testGetPublicKeyPem() {
        String pem = authService.getPublicKeyPem();
        assertNotNull(pem);
        assertTrue(pem.startsWith("-----BEGIN PUBLIC KEY-----"));
    }
}
