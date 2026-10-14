package com.example.msm_user.dto;

import java.io.Serializable;

/**
 * DTO for updating existing user details.
 */
public class UserUpdateDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String role;
    private Boolean active;

    public UserUpdateDto() {
    }

    public UserUpdateDto(String name, String role, Boolean active) {
        this.name = name;
        this.role = role;
        this.active = active;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
