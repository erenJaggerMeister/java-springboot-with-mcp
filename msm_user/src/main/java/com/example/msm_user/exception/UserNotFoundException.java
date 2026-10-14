package com.example.msm_user.exception;

import com.example.library_helper.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Exception thrown when a user cannot be found.
 */
public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UUID id) {
        super("User", "id", id);
    }

    public UserNotFoundException(String loginId) {
        super("User", "loginId", loginId);
    }
}
