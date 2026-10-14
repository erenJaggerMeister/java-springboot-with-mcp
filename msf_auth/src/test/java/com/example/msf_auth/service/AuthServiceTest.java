package com.example.msf_auth.service;

import com.example.library_helper.security.JwtTokenService;
import com.example.library_helper.security.RsaKeyProperties;
import com.example.library_helper.security.RsaKeyProvider;
import com.example.msf_auth.client.MsmUserClient;
import com.example.msf_auth.dto.LoginRequestDto;
import com.example.msf_auth.dto.LoginResponseDto;
import com.example.msf_auth.dto.SignUpRequestDto;
import com.example.msf_auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MsmUserClient msmUserClient;

    private PasswordEncoder passwordEncoder;
    private RsaKeyProvider rsaKeyProvider;
    private RsaKeyProperties rsaKeyProperties;
    private JwtTokenService jwtTokenService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        rsaKeyProperties = new RsaKeyProperties();
        rsaKeyProperties.setExpirationMinutes(60);
        rsaKeyProvider = new RsaKeyProvider(rsaKeyProperties);
        rsaKeyProvider.init();
        jwtTokenService = new JwtTokenService(rsaKeyProvider, rsaKeyProperties);

        authService = new AuthServiceImpl(msmUserClient, passwordEncoder, jwtTokenService, rsaKeyProvider, rsaKeyProperties);
    }

    @Test
    void testSignUpHashesPasswordAndCallsClient() {
        SignUpRequestDto request = new SignUpRequestDto("marcellius", "password123", "pengguna");

        when(msmUserClient.createUser(anyMap())).thenAnswer(invocation -> {
            Map<String, Object> map = invocation.getArgument(0);
            assertEquals("marcellius", map.get("name"));
            String hashed = (String) map.get("password");
            assertNotNull(hashed);
            assertTrue(hashed.startsWith("$2a$"));
            assertTrue(passwordEncoder.matches("password123", hashed));
            return map;
        });

        Map<String, Object> result = authService.signUp(request);
        assertNotNull(result);
        assertEquals("marcellius", result.get("name"));
        verify(msmUserClient, times(1)).createUser(anyMap());
    }

    @Test
    void testLoginSuccess() {
        String hashed = passwordEncoder.encode("password123");
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", 1L);
        userMap.put("name", "marcellius");
        userMap.put("loginId", "marcellius");
        userMap.put("password", hashed);
        userMap.put("role", "ROLE_USER");
        userMap.put("active", true);

        when(msmUserClient.findUser("marcellius")).thenReturn(userMap);

        LoginRequestDto request = new LoginRequestDto("marcellius", "password123");
        LoginResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertNotNull(response.getAccessToken());
        assertEquals("marcellius", response.getLoginId());
        assertEquals("marcellius", response.getName());
    }
}
