package com.example.msm_user.mapper;

import com.example.msm_user.dto.UserCreateDto;
import com.example.msm_user.dto.UserResponseDto;
import com.example.msm_user.dto.UserUpdateDto;
import com.example.msm_user.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper for converting between User entity and DTOs.
 */
@Component
public class UserMapper {

    public User toEntity(UserCreateDto dto, String password) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setLoginId(dto.getLoginId());
        user.setPassword(password);
        user.setName(dto.getName());
        user.setRole(dto.getRole() != null ? dto.getRole() : "pengguna");
        user.setActive(true);
        user.setCreatedBy(dto.getCreatedBy());
        user.setCreatedDate(dto.getCreatedDate() != null ? dto.getCreatedDate() : LocalDateTime.now());
        return user;
    }

    public void updateEntity(User user, UserUpdateDto dto) {
        if (user == null || dto == null) {
            return;
        }

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getActive() != null) {
            user.setActive(dto.getActive());
        }
    }

    public UserResponseDto toResponseDto(User entity) {
        if (entity == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(entity.getId())
                .loginId(entity.getLoginId())
                .name(entity.getName())
                .role(entity.getRole())
                .active(entity.getActive())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
