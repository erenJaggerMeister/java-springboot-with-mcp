package com.example.library_helper.security;

import com.example.library_helper.exception.UnauthorizedException;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private RsaKeyProvider rsaKeyProvider;
    private RsaKeyProperties properties;
    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        properties = new RsaKeyProperties();
        properties.setKeyId("test-rsa-key");
        properties.setIssuer("test-issuer");
        properties.setExpirationMinutes(60);

        rsaKeyProvider = new RsaKeyProvider(properties);
        rsaKeyProvider.init();

        jwtTokenService = new JwtTokenService(rsaKeyProvider, properties);
    }

    @Test
    void testGenerateAndValidateTokenSuccess() {
        String subject = "1";
        String loginId = "marcellius@example.com";
        List<String> roles = Collections.singletonList("ROLE_ADMIN");

        String token = jwtTokenService.generateToken(subject, loginId, roles);

        assertNotNull(token);
        assertFalse(token.isBlank());

        SignedJWT signedJWT = jwtTokenService.validateToken(token);
        assertNotNull(signedJWT);

        assertEquals(subject, jwtTokenService.getSubject(token));
        assertEquals(loginId, jwtTokenService.getLoginId(token));
        assertEquals(roles, jwtTokenService.getRoles(token));
    }

    @Test
    void testValidateTamperedTokenThrowsUnauthorized() {
        String token = jwtTokenService.generateToken("1", "marcellius@example.com", List.of("ROLE_USER"));
        String tampered = token.substring(0, token.length() - 5) + "abcde";

        assertThrows(UnauthorizedException.class, () -> jwtTokenService.validateToken(tampered));
    }

    @Test
    void testExportPublicKeyPem() {
        String pem = rsaKeyProvider.getPublicKeyPem();
        assertNotNull(pem);
        assertTrue(pem.startsWith("-----BEGIN PUBLIC KEY-----"));
        assertTrue(pem.endsWith("-----END PUBLIC KEY-----"));
    }
}
