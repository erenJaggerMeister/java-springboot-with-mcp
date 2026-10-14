package com.example.msf_auth.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Response payload containing Bearer token and user details.
 */
public class LoginResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tokenType = "Bearer";
    private String accessToken;
    private long expiresIn;
    private String loginId;
    private String name;
    private List<String> roles;

    public LoginResponseDto() {
    }

    public LoginResponseDto(String tokenType, String accessToken, long expiresIn, String loginId, String name, List<String> roles) {
        this.tokenType = tokenType != null ? tokenType : "Bearer";
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.loginId = loginId;
        this.name = name;
        this.roles = roles;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public static class Builder {
        private String tokenType = "Bearer";
        private String accessToken;
        private long expiresIn;
        private String loginId;
        private String name;
        private List<String> roles;

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public Builder expiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public Builder loginId(String loginId) {
            this.loginId = loginId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        public LoginResponseDto build() {
            return new LoginResponseDto(tokenType, accessToken, expiresIn, loginId, name, roles);
        }
    }
}
