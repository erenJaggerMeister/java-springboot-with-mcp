package com.example.library_helper.security;

import com.example.library_helper.exception.UnauthorizedException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service for issuing and verifying JWT Bearer tokens using Nimbus JOSE + JWT with RSA-2048 (RS256).
 */
@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);

    private final RsaKeyProvider rsaKeyProvider;
    private final RsaKeyProperties properties;

    public JwtTokenService(RsaKeyProvider rsaKeyProvider, RsaKeyProperties properties) {
        this.rsaKeyProvider = rsaKeyProvider;
        this.properties = properties;
    }

    /**
     * Issues a signed Bearer JWT token using RSA-2048 private key.
     *
     * @param subject Subject (user identifier)
     * @param loginId User login ID (e.g. marcellius@example.com)
     * @param roles   Assigned authorities/roles
     * @return Signed Bearer JWT token string
     */
    public String generateToken(String subject, String loginId, List<String> roles) {
        if (rsaKeyProvider.getPrivateKey() == null) {
            throw new IllegalStateException("RSA Private Key is required to sign JWT tokens");
        }

        try {
            JWSSigner signer = new RSASSASigner(rsaKeyProvider.getPrivateKey());
            Instant now = Instant.now();
            Instant expiration = now.plus(properties.getExpirationMinutes(), ChronoUnit.MINUTES);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(properties.getIssuer())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("login_id", loginId)
                    .claim("roles", roles != null ? roles : Collections.emptyList())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(properties.getKeyId())
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);

            String token = signedJWT.serialize();
            log.info("Successfully issued Bearer token for login_id: {} with expiration: {}", loginId, expiration);
            return token;
        } catch (Exception e) {
            log.error("Failed to sign JWT token for login_id {}: {}", loginId, e.getMessage(), e);
            throw new RuntimeException("Error signing JWT token", e);
        }
    }

    /**
     * Validates a JWT Bearer token using RSA-2048 public key.
     *
     * @param token JWT token string
     * @return Verified SignedJWT
     */
    public SignedJWT validateToken(String token) {
        if (rsaKeyProvider.getPublicKey() == null) {
            throw new IllegalStateException("RSA Public Key is required to verify JWT tokens");
        }

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new RSASSAVerifier(rsaKeyProvider.getPublicKey());

            if (!signedJWT.verify(verifier)) {
                log.warn("JWT token signature verification failed");
                throw new UnauthorizedException("Invalid token signature");
            }

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            if (expirationTime != null && new Date().after(expirationTime)) {
                log.warn("JWT token has expired at {}", expirationTime);
                throw new UnauthorizedException("Token has expired");
            }

            return signedJWT;
        } catch (ParseException e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
            throw new UnauthorizedException("Malformed JWT token");
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("JWT token verification error: {}", e.getMessage());
            throw new UnauthorizedException("Token verification error: " + e.getMessage());
        }
    }

    public String getSubject(String token) {
        SignedJWT jwt = validateToken(token);
        try {
            return jwt.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new UnauthorizedException("Cannot extract subject from token");
        }
    }

    public String getLoginId(String token) {
        SignedJWT jwt = validateToken(token);
        try {
            return jwt.getJWTClaimsSet().getStringClaim("login_id");
        } catch (ParseException e) {
            throw new UnauthorizedException("Cannot extract login_id from token");
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        SignedJWT jwt = validateToken(token);
        try {
            List<String> roles = (List<String>) jwt.getJWTClaimsSet().getClaim("roles");
            return roles != null ? roles : Collections.emptyList();
        } catch (ParseException e) {
            throw new UnauthorizedException("Cannot extract roles from token");
        }
    }

    public Date getExpiration(String token) {
        SignedJWT jwt = validateToken(token);
        try {
            return jwt.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException e) {
            throw new UnauthorizedException("Cannot extract expiration from token");
        }
    }
}
