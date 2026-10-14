package com.example.msf_user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * Request payload for creating a new user through msf_user orchestrator.
 * Expected JSON: {"name": "john_doe", "loginId": "john_doe@example.com", "password": "password123"}
 */
public class CreateUserRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Login ID is required")
    private String loginId;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String role = "pengguna";

    public CreateUserRequestDto() {
    }

    public CreateUserRequestDto(String name, String loginId, String password) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.role = "pengguna";
    }

    public CreateUserRequestDto(String name, String loginId, String password, String role) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.role = (role != null && !role.isBlank()) ? role : "pengguna";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = (role != null && !role.isBlank()) ? role : "pengguna";
    }
}
