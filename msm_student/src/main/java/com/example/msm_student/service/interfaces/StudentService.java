package com.example.msm_student.service.interfaces;

import com.example.library_helper.dto.PagingResult;
import com.example.msm_student.dto.StudentCreateDto;
import com.example.msm_student.dto.StudentResponseDto;
import com.example.msm_student.dto.StudentUpdateDto;

import java.util.UUID;

/**
 * Service interface for Student operations following SOLID principles with UUID key.
 */
public interface StudentService {

    /**
     * Creates a new Student record.
     */
    StudentResponseDto create(StudentCreateDto dto);

    /**
     * Retrieves a Student by ID.
     */
    StudentResponseDto getById(UUID id);

    /**
     * Retrieves paginated Student records with optional filtering.
     */
    PagingResult<StudentResponseDto> getPaging(int page, int size, String sortBy, String sortDir,
                                              String nameFilter, String matchMode);

    /**
     * Updates an existing Student record.
     */
    StudentResponseDto update(UUID id, StudentUpdateDto dto);

    /**
     * Deletes a Student record by ID.
     */
    void delete(UUID id);
}
