package com.example.msm_student.service.impl;

import com.example.library_helper.dto.PagingResult;
import com.example.msm_student.dto.StudentCreateDto;
import com.example.msm_student.dto.StudentResponseDto;
import com.example.msm_student.dto.StudentUpdateDto;
import com.example.msm_student.exception.StudentNotFoundException;
import com.example.msm_student.mapper.StudentMapper;
import com.example.msm_student.model.Student;
import com.example.msm_student.repository.StudentRepository;
import com.example.msm_student.security.StudentSecurityContext;
import com.example.msm_student.service.interfaces.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for Student management operations with comprehensive activity logging, tracing, and UUID identifiers.
 */
@Service
public class StudentServiceImpl implements StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final StudentSecurityContext securityContext;

    public StudentServiceImpl(StudentRepository studentRepository,
                              StudentMapper studentMapper,
                              StudentSecurityContext securityContext) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
        this.securityContext = securityContext;
    }

    @Override
    @Transactional
    public StudentResponseDto create(StudentCreateDto dto) {
        String caller = securityContext.getCurrentUserLoginId();
        log.info("[STUDENT-CREATE] User '{}' requested creating student: nama='{}', umur={}, kelas='{}', jenisKelas='{}'",
                caller, dto.getNama(), dto.getUmur(), dto.getKelas(), dto.getJenisKelas());

        Student student = studentMapper.toEntity(dto);
        Student saved = studentRepository.save(student);

        log.info("[STUDENT-CREATED] User '{}' successfully created student with ID: {} for nama='{}'",
                caller, saved.getId(), saved.getNama());

        return studentMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponseDto getById(UUID id) {
        String caller = securityContext.getCurrentUserLoginId();
        log.info("[STUDENT-GET] User '{}' requested student details for ID: {}", caller, id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[STUDENT-NOT-FOUND] User '{}' requested non-existent student ID: {}", caller, id);
                    return new StudentNotFoundException(id);
                });

        log.debug("[STUDENT-GET-SUCCESS] Retrieved student ID: {}, nama: '{}', kelas: '{}'",
                student.getId(), student.getNama(), student.getKelas());

        return studentMapper.toResponseDto(student);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResult<StudentResponseDto> getPaging(int page, int size, String sortBy, String sortDir,
                                                    String nameFilter, String matchMode) {
        String caller = securityContext.getCurrentUserLoginId();
        log.info("[STUDENT-PAGING] User '{}' requested paginated students: page={}, size={}, sortBy='{}', sortDir='{}', nameFilter='{}', matchMode='{}'",
                caller, page, size, sortBy, sortDir, nameFilter, matchMode);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Student> studentPage;
        if (StringUtils.hasText(nameFilter)) {
            studentPage = studentRepository.findByNamaContainingIgnoreCase(nameFilter.trim(), pageable);
        } else {
            studentPage = studentRepository.findAll(pageable);
        }

        List<StudentResponseDto> content = studentPage.getContent().stream()
                .map(studentMapper::toResponseDto)
                .collect(Collectors.toList());

        log.info("[STUDENT-PAGING-SUCCESS] User '{}' fetched page {}/{} ({} items on current page, {} total elements)",
                caller, studentPage.getNumber(), studentPage.getTotalPages(), content.size(), studentPage.getTotalElements());

        return PagingResult.from(studentPage, content);
    }

    @Override
    @Transactional
    public StudentResponseDto update(UUID id, StudentUpdateDto dto) {
        String caller = securityContext.getCurrentUserLoginId();
        log.info("[STUDENT-UPDATE] User '{}' requested updating student ID: {}", caller, id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("[STUDENT-UPDATE-FAILED] Cannot update non-existent student ID: {} requested by '{}'", id, caller);
                    return new StudentNotFoundException(id);
                });

        studentMapper.updateEntity(student, dto);
        Student updated = studentRepository.save(student);

        log.info("[STUDENT-UPDATED] User '{}' successfully updated student ID: {}", caller, updated.getId());

        return studentMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        String caller = securityContext.getCurrentUserLoginId();
        log.info("[STUDENT-DELETE] User '{}' requested deleting student ID: {}", caller, id);

        if (!studentRepository.existsById(id)) {
            log.warn("[STUDENT-DELETE-FAILED] Cannot delete non-existent student ID: {} requested by '{}'", id, caller);
            throw new StudentNotFoundException(id);
        }

        studentRepository.deleteById(id);
        log.info("[STUDENT-DELETED] User '{}' successfully deleted student ID: {}", caller, id);
    }
}
