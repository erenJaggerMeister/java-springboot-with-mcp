package com.example.library_helper.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for RSA-2048 key pair and JWT settings.
 * Supports direct PEM string, external file path, or environment variables.
 */
@Component
@ConfigurationProperties(prefix = "msm.security.rsa")
public class RsaKeyProperties {

    /**
     * RSA Private Key in PEM or Base64 PKCS#8 format.
     */
    private String privateKey;

    /**
     * File path to external RSA Private Key file (e.g. /etc/secrets/private_key.pem).
     */
    private String privateKeyPath;

    /**
     * RSA Public Key in PEM or Base64 X.509 format.
     */
    private String publicKey;

    /**
     * File path to external RSA Public Key file (e.g. /etc/secrets/public_key.pem).
     */
    private String publicKeyPath;

    /**
     * HTTP URL to dynamically retrieve RSA Public Key from auth server (e.g. http://localhost:8080/api/v1/auth/public-key).
     */
    private String publicKeyUrl;

    /**
     * Unique Key ID for JOSE header.
     */
    private String keyId = "msm-rsa-2048-key";

    /**
     * JWT Issuer.
     */
    private String issuer = "msf-auth-service";

    /**
     * Token expiration time in minutes (default 60 minutes).
     */
    private long expirationMinutes = 60;

    public RsaKeyProperties() {
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public String getPublicKeyUrl() {
        return publicKeyUrl;
    }

    public void setPublicKeyUrl(String publicKeyUrl) {
        this.publicKeyUrl = publicKeyUrl;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(long expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }
}
