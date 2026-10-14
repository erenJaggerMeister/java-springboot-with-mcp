package com.example.msm_user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Request payload for authentication.
 */
public class LoginRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Login ID is required")
    @Email(message = "Login ID must be a valid email address")
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
