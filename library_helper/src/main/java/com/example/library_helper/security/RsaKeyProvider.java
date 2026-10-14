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

    private static final String DEV_KEY_FILE = System.getProperty("java.io.tmpdir") + File.separator + "msm-rsa-dev-keypair.properties";

    @PostConstruct
    public void init() {
        try {
            String privContent = resolveKeyContent(properties.getPrivateKey(), properties.getPrivateKeyPath());
            String pubContent = resolveKeyContent(properties.getPublicKey(), properties.getPublicKeyPath());

            if (!StringUtils.hasText(pubContent) && StringUtils.hasText(properties.getPublicKeyUrl())) {
                pubContent = fetchPublicKeyFromUrl(properties.getPublicKeyUrl());
            }

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
                initDevKeyPair();
            }
        } catch (Exception e) {
            log.error("Failed to initialize RSA keys: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize RSA keys", e);
        }
    }

    private void initDevKeyPair() {
        File devFile = new File(DEV_KEY_FILE);
        if (devFile.exists()) {
            try {
                java.util.Properties props = new java.util.Properties();
                try (java.io.InputStream in = Files.newInputStream(devFile.toPath())) {
                    props.load(in);
                }
                String priv = props.getProperty("private.key");
                String pub = props.getProperty("public.key");
                if (StringUtils.hasText(priv) && StringUtils.hasText(pub)) {
                    log.info("Loaded shared development RSA-2048 key pair from: {}", DEV_KEY_FILE);
                    this.privateKey = parsePrivateKey(priv);
                    this.publicKey = parsePublicKey(pub);
                    return;
                }
            } catch (Exception e) {
                log.warn("Failed to load shared dev key file: {}. Regenerating...", e.getMessage());
            }
        }

        log.warn("No RSA-2048 keys configured via environment or path. Generating an RSA-2048 key pair for development...");
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();
            this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
            this.publicKey = (RSAPublicKey) keyPair.getPublic();

            java.util.Properties props = new java.util.Properties();
            props.setProperty("private.key", getPrivateKeyPem());
            props.setProperty("public.key", getPublicKeyPem());
            try (java.io.OutputStream out = Files.newOutputStream(devFile.toPath())) {
                props.store(out, "Shared RSA Dev Key Pair for MSM Ecosystem");
            }
            log.info("Generated and saved shared RSA-2048 dev key pair to: {}", DEV_KEY_FILE);
        } catch (Exception e) {
            log.warn("Could not persist dev key pair to file: {}. Falling back to in-memory key pair.", e.getMessage());
        }
    }

    public String fetchPublicKeyFromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        try {
            log.info("Attempting to fetch RSA public key from auth server URL: {}", url);
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(2))
                    .build();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(3))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && StringUtils.hasText(resp.body())) {
                log.info("Successfully fetched RSA public key from {}", url);
                return resp.body().trim();
            }
        } catch (Exception e) {
            log.warn("Could not fetch RSA public key from URL {}: {}", url, e.getMessage());
        }
        return null;
    }

    public boolean reloadPublicKeyFromUrl() {
        if (StringUtils.hasText(properties.getPublicKeyUrl())) {
            String fetched = fetchPublicKeyFromUrl(properties.getPublicKeyUrl());
            if (StringUtils.hasText(fetched)) {
                try {
                    this.publicKey = parsePublicKey(fetched);
                    log.info("Successfully reloaded RSA public key from {}", properties.getPublicKeyUrl());
                    return true;
                } catch (Exception e) {
                    log.error("Failed to parse reloaded RSA public key: {}", e.getMessage());
                }
            }
        }
        return false;
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

    public String getPrivateKeyPem() {
        if (privateKey == null) {
            return null;
        }
        String base64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" +
                base64.replaceAll("(.{64})", "$1\n").trim() +
                "\n-----END PRIVATE KEY-----";
    }
}
