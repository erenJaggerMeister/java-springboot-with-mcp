package com.example.msm_user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for creating a new user in msm_user.
 */
public class UserCreateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String loginId;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    private String role = "pengguna";

    private String createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdDate;

    public UserCreateDto() {
    }

    public UserCreateDto(String loginId, String password, String name, String role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role != null ? role : "pengguna";
        this.createdDate = LocalDateTime.now();
    }

    public UserCreateDto(String name, String password, String role, String createdBy, LocalDateTime createdDate) {
        this.name = name;
        this.loginId = name;
        this.password = password;
        this.role = role != null ? role : "pengguna";
        this.createdBy = createdBy;
        this.createdDate = createdDate != null ? createdDate : LocalDateTime.now();
    }

    public String getLoginId() {
        return loginId != null ? loginId : name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
