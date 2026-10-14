package com.example.msf_auth.dto;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Request payload for user login in msf_auth.
 */
public class LoginRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Login ID or Username is required")
    private String loginId;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String loginId, String password) {
        this.loginId = loginId;
        this.password = password;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
