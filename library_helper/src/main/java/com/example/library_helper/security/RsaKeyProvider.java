package com.example.library_helper.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Provider and manager for RSA-2048 asymmetric key pair.
 * Loads keys from string properties, external files, or auto-generates ephemeral keys in dev.
 */
@Component
public class RsaKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(RsaKeyProvider.class);

    private final RsaKeyProperties properties;
    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public RsaKeyProvider(RsaKeyProperties properties) {
        this.properties = properties;
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    @PostConstruct
    public void init() {
        try {
            String privContent = resolveKeyContent(properties.getPrivateKey(), properties.getPrivateKeyPath());
            String pubContent = resolveKeyContent(properties.getPublicKey(), properties.getPublicKeyPath());

            boolean hasPriv = StringUtils.hasText(privContent);
            boolean hasPub = StringUtils.hasText(pubContent);

            if (hasPriv && hasPub) {
                log.info("Loading configured RSA-2048 asymmetric key pair...");
                this.privateKey = parsePrivateKey(privContent);
                this.publicKey = parsePublicKey(pubContent);
                log.info("RSA-2048 key pair loaded successfully.");
            } else if (hasPub) {
                log.info("Loading configured RSA-2048 public key for verification...");
                this.publicKey = parsePublicKey(pubContent);
            } else {
                log.warn("No RSA-2048 keys configured via environment or path. Generating an ephemeral in-memory RSA-2048 key pair for development...");
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
                generator.initialize(2048);
                KeyPair keyPair = generator.generateKeyPair();
                this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
                this.publicKey = (RSAPublicKey) keyPair.getPublic();
                log.info("Ephemeral RSA-2048 key pair generated successfully.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize RSA keys: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize RSA keys", e);
        }
    }

    private String resolveKeyContent(String rawValue, String filePath) {
        if (StringUtils.hasText(rawValue)) {
            return rawValue;
        }
        if (StringUtils.hasText(filePath)) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    log.info("Reading RSA key from file path: {}", filePath);
                    return Files.readString(file.toPath());
                } else {
                    log.warn("Specified RSA key file not found: {}", filePath);
                }
            } catch (Exception e) {
                log.error("Error reading RSA key file from {}: {}", filePath, e.getMessage());
            }
        }
        return null;
    }

    public static RSAPrivateKey parsePrivateKey(String pemOrBase64) throws Exception {
        String cleaned = cleanKey(pemOrBase64, "PRIVATE KEY");
        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) factory.generatePrivate(spec);
    }

    public static RSAPublicKey parsePublicKey(String pemOrBase64) throws Exception {
        String cleaned = cleanKey(pemOrBase64, "PUBLIC KEY");
        byte[] keyBytes = Base64.getDecoder().decode(cleaned);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) factory.generatePublic(spec);
    }

    private static String cleanKey(String raw, String keyType) {
        return raw.replaceAll("-----BEGIN " + keyType + "-----", "")
                .replaceAll("-----END " + keyType + "-----", "")
                .replaceAll("\\s+", "");
    }

    public String getPublicKeyPem() {
        if (publicKey == null) {
            return null;
        }
        String base64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" +
                base64.replaceAll("(.{64})", "$1\n").trim() +
                "\n-----END PUBLIC KEY-----";
    }
}
