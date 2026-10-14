package com.example.msf_student.service.interfaces;

import com.example.library_helper.dto.PagingResult;
import com.example.msf_student.dto.StudentDto;
import com.example.msf_student.dto.StudentRequestDto;

import java.util.UUID;

/**
 * Service interface for Student Orchestration in msf_student returning strongly-typed StudentDto.
 */
public interface StudentOrchestrationService {

    /**
     * Retrieves paginated students forwarding authentication Bearer token and search filters.
     */
    PagingResult<StudentDto> getPaging(int page, int rows, String sortBy, String sortDir,
                                       String nameFilter, String matchMode, String bearerToken);

    StudentDto create(StudentRequestDto payload, String bearerToken);

    StudentDto getById(UUID id, String bearerToken);

    StudentDto update(UUID id, StudentRequestDto payload, String bearerToken);

    void delete(UUID id, String bearerToken);
}
