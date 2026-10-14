package com.example.msf_user.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.dto.PagingResult;
import com.example.msf_user.dto.CreateUserRequestDto;
import com.example.msf_user.dto.UserDto;
import com.example.msf_user.service.interfaces.UserOrchestrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing user orchestration endpoints in msf_user returning strongly-typed UserDto and PagingResult<UserDto>.
 */
@RestController
@RequestMapping
public class UserOrchestrationController {

    private static final Logger log = LoggerFactory.getLogger(UserOrchestrationController.class);

    private final UserOrchestrationService userService;

    public UserOrchestrationController(UserOrchestrationService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint for 'super_admin' to add a new user.
     * Request body: {"name": "john_doe", "loginId": "john_doe@example.com", "password": "password123"}
     */
    @PostMapping("/api/msf_user/create/newUser")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            @Valid @RequestBody CreateUserRequestDto request,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST request to /api/msf_user/create/newUser for user: '{}', loginId: '{}'",
                request.getName(), request.getLoginId());
        UserDto created = userService.createUser(request, bearerToken);
        return new ResponseEntity<>(ApiResponse.success(created, "User created successfully by super_admin"), HttpStatus.CREATED);
    }

    /**
     * Endpoint for paginated user data retrieval from msm_user returning PagingResult<UserDto>.
     */
    @GetMapping("/api/msf_user/getDataUser/paging")
    public ResponseEntity<ApiResponse<PagingResult<UserDto>>> getPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST request to /api/msf_user/getDataUser/paging: page={}, rows={}", page, rows);
        PagingResult<UserDto> result = userService.getPaging(page, rows, sortBy, sortDir, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(result, "Users retrieved successfully"));
    }
}
