package com.example.msf_user.service.impl;

import com.example.library_helper.dto.PagingResult;
import com.example.msf_user.client.MsmUserClient;
import com.example.msf_user.dto.CreateUserRequestDto;
import com.example.msf_user.dto.UserDto;
import com.example.msf_user.service.interfaces.UserOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service implementation for User Orchestration in msf_user.
 * Enforces role 'super_admin', extracts current loginId as createdBy,
 * applies Bangkok timezone to createdDate, hashes password in msf_user, and returns UserDto.
 */
@Service
public class UserOrchestrationServiceImpl implements UserOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(UserOrchestrationServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd'T'HH:mm:ss.SSS");
    private static final ZoneId BANGKOK_ZONE = ZoneId.of("Asia/Bangkok");

    private final MsmUserClient msmUserClient;
    private final PasswordEncoder passwordEncoder;

    public UserOrchestrationServiceImpl(MsmUserClient msmUserClient, PasswordEncoder passwordEncoder) {
        this.msmUserClient = msmUserClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(CreateUserRequestDto request, String bearerToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentLoginId = (auth != null && auth.getName() != null) ? auth.getName() : "ANONYMOUS";

        log.info("[MSF-USER-CREATE] Caller '{}' attempting to create new user: name='{}', loginId='{}', role='{}'",
                currentLoginId, request.getName(), request.getLoginId(), request.getRole());

        // Enforce that only 'super_admin' or 'ROLE_SUPER_ADMIN' can create users
        boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("super_admin") ||
                               a.getAuthority().equalsIgnoreCase("ROLE_super_admin") ||
                               a.getAuthority().equalsIgnoreCase("ROLE_SUPER_ADMIN"));

        if (!isSuperAdmin) {
            log.warn("[MSF-USER-CREATE-FORBIDDEN] Access denied: Caller '{}' is not a super_admin", currentLoginId);
            throw new AccessDeniedException("Access Denied: Only users with role 'super_admin' can add new users");
        }

        // Hash raw password with BCrypt in msf_user
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        log.debug("[MSF-USER-CREATE] Password successfully hashed with BCrypt");

        // Obtain current time in Asia/Bangkok time zone
        LocalDateTime createdDateBangkok = LocalDateTime.now(BANGKOK_ZONE);
        String formattedBangkokDate = createdDateBangkok.format(DATE_FORMATTER);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", request.getName());
        payload.put("loginId", request.getLoginId());
        payload.put("password", hashedPassword);
        payload.put("role", request.getRole() != null ? request.getRole() : "pengguna");
        payload.put("createdBy", currentLoginId);
        payload.put("createdDate", formattedBangkokDate);

        log.info("[MSF-USER-CREATE] Delegating user persistence to msm_user: createdBy='{}', createdDateBangkok='{}'",
                currentLoginId, formattedBangkokDate);

        return msmUserClient.createUser(payload, bearerToken);
    }

    @Override
    public PagingResult<UserDto> getPaging(int page, int rows, String sortBy, String sortDir, String bearerToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentCaller = auth != null ? auth.getName() : "ANONYMOUS";

        log.info("[MSF-USER-PAGING] Caller '{}' fetching user paging: page={}, rows={}", currentCaller, page, rows);
        return msmUserClient.getPaging(page, rows, sortBy, sortDir, bearerToken);
    }
}
