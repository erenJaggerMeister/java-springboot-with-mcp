package com.example.msm_user.service.interfaces;

import com.example.library_helper.dto.PagingResult;
import com.example.msm_user.dto.UserCreateDto;
import com.example.msm_user.dto.UserResponseDto;
import com.example.msm_user.dto.UserUpdateDto;

import java.util.UUID;

/**
 * Service interface for User CRUD operations.
 */
public interface UserService {

    /**
     * Creates a new user with BCrypt password hashing.
     */
    UserResponseDto create(UserCreateDto dto);

    /**
     * Retrieves a user by ID.
     */
    UserResponseDto getById(UUID id);

    /**
     * Retrieves a user by login ID (email).
     */
    UserResponseDto getByLoginId(String loginId);

    /**
     * Retrieves a paginated list of users.
     */
    PagingResult<UserResponseDto> getAll(int page, int size, String sortBy, String sortDir);

    /**
     * Updates an existing user.
     */
    UserResponseDto update(UUID id, UserUpdateDto dto);

    /**
     * Deletes a user by ID.
     */
    void delete(UUID id);
}
