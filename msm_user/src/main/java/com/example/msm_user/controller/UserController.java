package com.example.msm_user.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.dto.PagingResult;
import com.example.msm_user.dto.UserCreateDto;
import com.example.msm_user.dto.UserResponseDto;
import com.example.msm_user.dto.UserUpdateDto;
import com.example.msm_user.model.User;
import com.example.msm_user.repository.UserRepository;
import com.example.msm_user.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller exposing User CRUD operations and pagination with UUID keys.
 */
@RestController
@RequestMapping
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Endpoint invoked by msf_user or msf_auth to create a new user.
     */
    @PostMapping({"/api/msm_user/create/newUser", "/api/v1/users"})
    public ResponseEntity<ApiResponse<UserResponseDto>> create(@Valid @RequestBody UserCreateDto dto) {
        log.info("REST request to create user: name='{}', loginId='{}', role='{}'",
                dto.getName(), dto.getLoginId(), dto.getRole());
        UserResponseDto created = userService.create(dto);
        return new ResponseEntity<>(ApiResponse.success(created, "User created successfully"), HttpStatus.CREATED);
    }

    /**
     * Endpoint invoked to find user details for authentication in msf_auth.
     */
    @GetMapping({"/api/msm_user/findUser", "/api/v1/users/findByLoginId"})
    public ResponseEntity<ApiResponse<User>> findUser(@RequestParam String loginId) {
        log.info("REST request to find user by loginId: {}", loginId);
        User user = userRepository.findByLoginId(loginId)
                .orElseGet(() -> userRepository.findAll().stream()
                        .filter(u -> loginId.equalsIgnoreCase(u.getName()) || loginId.equalsIgnoreCase(u.getLoginId()))
                        .findFirst()
                        .orElse(null));

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
        }
        return ResponseEntity.ok(ApiResponse.success(user, "User found"));
    }

    @GetMapping({"/api/msm_user/getDataUser/paging", "/api/v1/users"})
    public ResponseEntity<ApiResponse<PagingResult<UserResponseDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(defaultValue = "10", required = false) Integer rows,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        int pageSize = rows != null ? rows : (size != null ? size : 10);
        log.info("REST request to get users paginated: page={}, size={}, sortBy={}, sortDir={}", page, pageSize, sortBy, sortDir);
        PagingResult<UserResponseDto> result = userService.getAll(page, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(result, "Users retrieved successfully"));
    }

    @GetMapping("/api/v1/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getById(@PathVariable UUID id) {
        log.info("REST request to get user by id: {}", id);
        UserResponseDto user = userService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(user, "User retrieved successfully"));
    }

    @PutMapping("/api/v1/users/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> update(@PathVariable UUID id, @RequestBody UserUpdateDto dto) {
        log.info("REST request to update user with id: {}", id);
        UserResponseDto updated = userService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "User updated successfully"));
    }

    @DeleteMapping("/api/v1/users/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        log.info("REST request to delete user with id: {}", id);
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
