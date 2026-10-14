package com.example.msm_student.controller;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.dto.PagingResult;
import com.example.msm_student.dto.StudentCreateDto;
import com.example.msm_student.dto.StudentResponseDto;
import com.example.msm_student.dto.StudentUpdateDto;
import com.example.msm_student.service.interfaces.StudentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller exposing Student CRUD and pagination operations with UUID identifiers.
 */
@RestController
@RequestMapping
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping({"/api/v1/students", "/api/msm_student/create"})
    public ResponseEntity<ApiResponse<StudentResponseDto>> create(@Valid @RequestBody StudentCreateDto dto) {
        log.info("REST endpoint invoked: POST student (nama: '{}', kelas: '{}')", dto.getNama(), dto.getKelas());
        StudentResponseDto created = studentService.create(dto);
        return new ResponseEntity<>(ApiResponse.success(created, "Student created successfully"), HttpStatus.CREATED);
    }

    @GetMapping({"/api/v1/students/{id}", "/api/msm_student/{id}"})
    public ResponseEntity<ApiResponse<StudentResponseDto>> getById(@PathVariable UUID id) {
        log.info("REST endpoint invoked: GET student by id: {}", id);
        StudentResponseDto student = studentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(student, "Student retrieved successfully"));
    }

    /**
     * Endpoint for paginated student retrieval with filter criteria.
     * Supports both /api/msm_student/getDataStudent/paging and /api/msm_user/getDataStudent/paging.
     */
    @GetMapping({"/api/msm_student/getDataStudent/paging", "/api/msm_user/getDataStudent/paging", "/api/v1/students"})
    public ResponseEntity<ApiResponse<PagingResult<StudentResponseDto>>> getPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(defaultValue = "10", required = false) Integer rows,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(name = "filters.name.value", required = false) String nameFilter,
            @RequestParam(name = "filters.name.matchMode", defaultValue = "contains") String matchMode) {

        int pageSize = rows != null ? rows : (size != null ? size : 10);
        log.info("REST endpoint invoked: getDataStudent/paging (page={}, rows={}, filterName='{}', matchMode='{}')",
                page, pageSize, nameFilter, matchMode);

        PagingResult<StudentResponseDto> result = studentService.getPaging(page, pageSize, sortBy, sortDir, nameFilter, matchMode);
        return ResponseEntity.ok(ApiResponse.success(result, "Students retrieved successfully"));
    }

    @PutMapping({"/api/v1/students/{id}", "/api/msm_student/{id}"})
    public ResponseEntity<ApiResponse<StudentResponseDto>> update(@PathVariable UUID id,
                                                                  @Valid @RequestBody StudentUpdateDto dto) {
        log.info("REST endpoint invoked: PUT update student id: {}", id);
        StudentResponseDto updated = studentService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Student updated successfully"));
    }

    @DeleteMapping({"/api/v1/students/{id}", "/api/msm_student/{id}"})
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        log.info("REST endpoint invoked: DELETE student id: {}", id);
        studentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Student deleted successfully"));
    }
}
