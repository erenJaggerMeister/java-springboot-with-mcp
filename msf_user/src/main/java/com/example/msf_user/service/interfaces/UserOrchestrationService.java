package com.example.msf_user.service.interfaces;

import com.example.library_helper.dto.PagingResult;
import com.example.msf_user.dto.CreateUserRequestDto;
import com.example.msf_user.dto.UserDto;

/**
 * Service interface for User Orchestration in msf_user returning strongly-typed UserDto objects.
 */
public interface UserOrchestrationService {

    /**
     * Creates a new user if the authenticated caller has the 'super_admin' role.
     * Takes loginId of the current user as created_by and sets created_date using Asia/Bangkok timezone.
     */
    UserDto createUser(CreateUserRequestDto request, String bearerToken);

    /**
     * Retrieves paginated users from msm_user.
     */
    PagingResult<UserDto> getPaging(int page, int rows, String sortBy, String sortDir, String bearerToken);
}
