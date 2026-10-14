package com.example.msm_user.service.impl;

import com.example.library_helper.dto.PagingResult;
import com.example.library_helper.exception.BadRequestException;
import com.example.msm_user.dto.UserCreateDto;
import com.example.msm_user.dto.UserResponseDto;
import com.example.msm_user.dto.UserUpdateDto;
import com.example.msm_user.exception.UserNotFoundException;
import com.example.msm_user.mapper.UserMapper;
import com.example.msm_user.model.User;
import com.example.msm_user.repository.UserRepository;
import com.example.msm_user.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of UserService for User CRUD operations with UUID identification.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponseDto create(UserCreateDto dto) {
        log.info("Creating new user with name: {}, login_id: {}, role: {}",
                dto.getName(), dto.getLoginId(), dto.getRole());

        if (dto.getLoginId() != null && userRepository.existsByLoginId(dto.getLoginId())) {
            log.warn("User creation rejected: Login ID '{}' already exists", dto.getLoginId());
            throw new BadRequestException("Login ID '" + dto.getLoginId() + "' is already registered");
        }

        // If password is not yet hashed, hash it with BCrypt; otherwise store hashed password
        String password = dto.getPassword();
        if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
            password = passwordEncoder.encode(password);
        }

        User user = userMapper.toEntity(dto, password);
        User saved = userRepository.save(user);

        log.info("User created successfully with id: {} and name: {}", saved.getId(), saved.getName());
        return userMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        log.info("Fetching user details for id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getByLoginId(String loginId) {
        log.info("Fetching user details for login_id: {}", loginId);
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UserNotFoundException(loginId));
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResult<UserResponseDto> getAll(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching paginated users: page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<User> userPage = userRepository.findAll(pageable);
        List<UserResponseDto> dtoList = userPage.getContent().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());

        log.info("Found {} users across {} total pages (total elements: {})",
                dtoList.size(), userPage.getTotalPages(), userPage.getTotalElements());

        return PagingResult.from(userPage, dtoList);
    }

    @Override
    @Transactional
    public UserResponseDto update(UUID id, UserUpdateDto dto) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userMapper.updateEntity(user, dto);
        User updated = userRepository.save(user);

        log.info("User with id: {} updated successfully", id);
        return userMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            log.warn("Cannot delete non-existent user with id: {}", id);
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("User with id: {} deleted successfully", id);
    }
}
