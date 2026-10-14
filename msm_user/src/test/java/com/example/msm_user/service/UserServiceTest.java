package com.example.msm_user.service;

import com.example.library_helper.dto.PagingResult;
import com.example.library_helper.exception.BadRequestException;
import com.example.msm_user.dto.UserCreateDto;
import com.example.msm_user.dto.UserResponseDto;
import com.example.msm_user.dto.UserUpdateDto;
import com.example.msm_user.exception.UserNotFoundException;
import com.example.msm_user.mapper.UserMapper;
import com.example.msm_user.model.User;
import com.example.msm_user.repository.UserRepository;
import com.example.msm_user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        userService = new UserServiceImpl(userRepository, passwordEncoder, userMapper);
    }

    @Test
    void testCreateUserSuccess() {
        UserCreateDto createDto = new UserCreateDto("newuser@example.com", "Secret123", "New User", "ROLE_USER");
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByLoginId("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("encoded_secret");

        User savedUser = new User(userId, "newuser@example.com", "encoded_secret", "New User", "ROLE_USER",
                true, LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDto response = userService.create(createDto);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("newuser@example.com", response.getLoginId());
        assertEquals("New User", response.getName());
    }

    @Test
    void testCreateDuplicateLoginIdThrowsBadRequest() {
        UserCreateDto createDto = new UserCreateDto("existing@example.com", "Secret123", "User", "ROLE_USER");
        when(userRepository.existsByLoginId("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.create(createDto));
    }

    @Test
    void testGetByIdFound() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user@example.com", "pwd", "User", "ROLE_USER", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.getById(userId);
        assertNotNull(response);
        assertEquals(userId, response.getId());
    }

    @Test
    void testGetByIdNotFoundThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userService.getById(userId));
    }

    @Test
    void testGetAllUsersPaging() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user@example.com", "pwd", "User", "ROLE_USER", true,
                LocalDateTime.now(), LocalDateTime.now());
        Page<User> page = new PageImpl<>(Collections.singletonList(user));
        when(userRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagingResult<UserResponseDto> result = userService.getAll(0, 10, "id", "asc");
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("user@example.com", result.getData().get(0).getLoginId());
    }

    @Test
    void testUpdateUser() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "user@example.com", "pwd", "Old Name", "ROLE_USER", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserUpdateDto updateDto = new UserUpdateDto("New Name", "ROLE_ADMIN", false);
        UserResponseDto updated = userService.update(userId, updateDto);

        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals("ROLE_ADMIN", updated.getRole());
        assertFalse(updated.getActive());
    }

    @Test
    void testDeleteUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);
        doNothing().when(userRepository).deleteById(userId);

        assertDoesNotThrow(() -> userService.delete(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }
}
