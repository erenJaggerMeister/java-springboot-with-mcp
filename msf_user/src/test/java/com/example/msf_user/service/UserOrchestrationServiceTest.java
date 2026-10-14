package com.example.msf_user.service;

import com.example.library_helper.dto.PagingResult;
import com.example.msf_user.client.MsmUserClient;
import com.example.msf_user.dto.CreateUserRequestDto;
import com.example.msf_user.dto.UserDto;
import com.example.msf_user.service.impl.UserOrchestrationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserOrchestrationServiceTest {

    @Mock
    private MsmUserClient msmUserClient;

    private BCryptPasswordEncoder passwordEncoder;
    private UserOrchestrationServiceImpl userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserOrchestrationServiceImpl(msmUserClient, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateUserBySuperAdminSuccess() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "superAdmin@example.com", null, List.of(new SimpleGrantedAuthority("super_admin")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        CreateUserRequestDto request = new CreateUserRequestDto("john_doe", "john_doe@example.com", "password123");
        UUID generatedId = UUID.randomUUID();

        when(msmUserClient.createUser(anyMap(), eq("Bearer token"))).thenAnswer(invocation -> {
            Map<String, Object> map = invocation.getArgument(0);
            assertEquals("john_doe", map.get("name"));
            assertEquals("john_doe@example.com", map.get("loginId"));
            assertEquals("superAdmin@example.com", map.get("createdBy"));
            assertNotNull(map.get("createdDate"));

            String hashed = (String) map.get("password");
            assertNotNull(hashed);
            assertTrue(passwordEncoder.matches("password123", hashed));

            return UserDto.builder()
                    .id(generatedId)
                    .name("john_doe")
                    .loginId("john_doe@example.com")
                    .role("pengguna")
                    .createdBy("superAdmin@example.com")
                    .createdDate(LocalDateTime.now())
                    .active(true)
                    .build();
        });

        UserDto result = userService.createUser(request, "Bearer token");
        assertNotNull(result);
        assertEquals(generatedId, result.getId());
        assertEquals("john_doe", result.getName());
        assertEquals("john_doe@example.com", result.getLoginId());
        assertEquals("superAdmin@example.com", result.getCreatedBy());
        verify(msmUserClient, times(1)).createUser(anyMap(), eq("Bearer token"));
    }

    @Test
    void testCreateUserByNonSuperAdminThrowsAccessDenied() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "regularUser@example.com", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        CreateUserRequestDto request = new CreateUserRequestDto("john_doe", "john_doe@example.com", "password123");

        assertThrows(AccessDeniedException.class, () -> userService.createUser(request, "Bearer token"));
        verify(msmUserClient, never()).createUser(anyMap(), any());
    }

    @Test
    void testGetPagingReturnsPagingResultOfUserDto() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "superAdmin@example.com", null, List.of(new SimpleGrantedAuthority("super_admin")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserDto userDto = UserDto.builder()
                .id(UUID.randomUUID())
                .name("john_doe")
                .loginId("john_doe@example.com")
                .role("pengguna")
                .active(true)
                .build();

        PagingResult<UserDto> mockPaging = PagingResult.<UserDto>builder()
                .data(Collections.singletonList(userDto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .build();

        when(msmUserClient.getPaging(0, 10, "id", "asc", "Bearer token"))
                .thenReturn(mockPaging);

        PagingResult<UserDto> result = userService.getPaging(0, 10, "id", "asc", "Bearer token");
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("john_doe", result.getData().get(0).getName());
        verify(msmUserClient, times(1)).getPaging(0, 10, "id", "asc", "Bearer token");
    }
}
