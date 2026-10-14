package com.example.msm_student.mapper;

import com.example.msm_student.dto.StudentCreateDto;
import com.example.msm_student.dto.StudentResponseDto;
import com.example.msm_student.dto.StudentUpdateDto;
import com.example.msm_student.model.Student;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Student entity and DTOs.
 */
@Component
public class StudentMapper {

    public Student toEntity(StudentCreateDto dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        student.setNama(dto.getNama());
        student.setUmur(dto.getUmur());
        student.setKelas(dto.getKelas());
        student.setJenisKelas(dto.getJenisKelas());
        return student;
    }

    public void updateEntity(Student entity, StudentUpdateDto dto) {
        if (entity == null || dto == null) {
            return;
        }

        if (dto.getNama() != null) {
            entity.setNama(dto.getNama());
        }
        if (dto.getUmur() != null) {
            entity.setUmur(dto.getUmur());
        }
        if (dto.getKelas() != null) {
            entity.setKelas(dto.getKelas());
        }
        if (dto.getJenisKelas() != null) {
            entity.setJenisKelas(dto.getJenisKelas());
        }
    }

    public StudentResponseDto toResponseDto(Student entity) {
        if (entity == null) {
            return null;
        }

        return StudentResponseDto.builder()
                .id(entity.getId())
                .nama(entity.getNama())
                .umur(entity.getUmur())
                .kelas(entity.getKelas())
                .jenisKelas(entity.getJenisKelas())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
