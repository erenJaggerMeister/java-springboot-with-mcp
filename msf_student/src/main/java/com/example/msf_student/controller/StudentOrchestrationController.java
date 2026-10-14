package com.example.msf_student.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.dto.PagingResult;
import com.example.msf_student.dto.StudentDto;
import com.example.msf_student.dto.StudentRequestDto;
import com.example.msf_student.service.interfaces.StudentOrchestrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller exposing student orchestration endpoints in msf_student.
 * Forwards requests with Bearer tokens to msm_student.
 * Returns strongly-typed StudentDto and PagingResult<StudentDto>.
 */
@RestController
@RequestMapping
public class StudentOrchestrationController {

    private static final Logger log = LoggerFactory.getLogger(StudentOrchestrationController.class);

    private final StudentOrchestrationService studentService;

    public StudentOrchestrationController(StudentOrchestrationService studentService) {
        this.studentService = studentService;
    }

    /**
     * Endpoint for getting student paging with filter criteria.
     * Example: /api/msf_student/getDataStudent/paging?page=0&rows=10&filters.name.value=marcellius&filters.name.matchMode=contains
     */
    @GetMapping({"/api/msf_student/getDataStudent/paging", "/api/msf_student/paging"})
    public ResponseEntity<ApiResponse<PagingResult<StudentDto>>> getPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int rows,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(name = "filters.name.value", required = false) String nameFilter,
            @RequestParam(name = "filters.name.matchMode", defaultValue = "contains") String matchMode,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {

        log.info("REST request to /api/msf_student/getDataStudent/paging: page={}, rows={}, filter='{}', matchMode='{}'",
                page, rows, nameFilter, matchMode);

        PagingResult<StudentDto> result = studentService.getPaging(
                page, rows, sortBy, sortDir, nameFilter, matchMode, bearerToken);

        return ResponseEntity.ok(ApiResponse.success(result, "Students retrieved successfully"));
    }

    @PostMapping("/api/msf_student/create")
    public ResponseEntity<ApiResponse<StudentDto>> create(
            @Valid @RequestBody StudentRequestDto payload,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST request to create student in msf_student: nama='{}'", payload.getNama());
        StudentDto created = studentService.create(payload, bearerToken);
        return new ResponseEntity<>(ApiResponse.success(created, "Student created successfully"), HttpStatus.CREATED);
    }

    @GetMapping("/api/msf_student/{id}")
    public ResponseEntity<ApiResponse<StudentDto>> getById(
            @PathVariable UUID id,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST request to get student ID: {}", id);
        StudentDto student = studentService.getById(id, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(student, "Student retrieved successfully"));
    }

    @PutMapping("/api/msf_student/{id}")
    public ResponseEntity<ApiResponse<StudentDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody StudentRequestDto payload,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST request to update student ID: {}", id);
        StudentDto updated = studentService.update(id, payload, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(updated, "Student updated successfully"));
    }

    @DeleteMapping("/api/msf_student/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String bearerToken) {
        log.info("REST request to delete student ID: {}", id);
        studentService.delete(id, bearerToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }
}
