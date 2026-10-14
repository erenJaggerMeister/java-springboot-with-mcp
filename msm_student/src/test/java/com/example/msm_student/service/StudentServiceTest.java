package com.example.msm_student.service;

import com.example.library_helper.dto.PagingResult;
import com.example.msm_student.dto.StudentCreateDto;
import com.example.msm_student.dto.StudentResponseDto;
import com.example.msm_student.dto.StudentUpdateDto;
import com.example.msm_student.exception.StudentNotFoundException;
import com.example.msm_student.mapper.StudentMapper;
import com.example.msm_student.model.Student;
import com.example.msm_student.repository.StudentRepository;
import com.example.msm_student.security.StudentSecurityContext;
import com.example.msm_student.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentSecurityContext securityContext;

    private StudentMapper studentMapper;
    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentMapper = new StudentMapper();
        studentService = new StudentServiceImpl(studentRepository, studentMapper, securityContext);
        lenient().when(securityContext.getCurrentUserLoginId()).thenReturn("marcellius@example.com");
    }

    @Test
    void testCreateStudentSuccess() {
        StudentCreateDto createDto = new StudentCreateDto("Budi Pratama", 17, "12-IPA-1", "Reguler");
        UUID studentId = UUID.randomUUID();

        Student savedStudent = new Student(studentId, "Budi Pratama", 17, "12-IPA-1", "Reguler",
                LocalDateTime.now(), LocalDateTime.now());

        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentResponseDto response = studentService.create(createDto);

        assertNotNull(response);
        assertEquals(studentId, response.getId());
        assertEquals("Budi Pratama", response.getNama());
        assertEquals(17, response.getUmur());
        assertEquals("12-IPA-1", response.getKelas());
        assertEquals("Reguler", response.getJenisKelas());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void testGetByIdFound() {
        UUID studentId = UUID.randomUUID();
        Student student = new Student(studentId, "Siti Rahma", 16, "11-IPS-2", "Akselerasi",
                LocalDateTime.now(), LocalDateTime.now());

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        StudentResponseDto response = studentService.getById(studentId);

        assertNotNull(response);
        assertEquals(studentId, response.getId());
        assertEquals("Siti Rahma", response.getNama());
    }

    @Test
    void testGetByIdNotFoundThrowsException() {
        UUID studentId = UUID.randomUUID();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(StudentNotFoundException.class, () -> studentService.getById(studentId));
    }

    @Test
    void testGetPagingWithFilter() {
        UUID studentId = UUID.randomUUID();
        Student student = new Student(studentId, "Marcellius", 17, "12-IPA-2", "Internasional",
                LocalDateTime.now(), LocalDateTime.now());
        Page<Student> page = new PageImpl<>(Collections.singletonList(student));

        when(studentRepository.findByNamaContainingIgnoreCase(eq("marcellius"), any(Pageable.class)))
                .thenReturn(page);

        PagingResult<StudentResponseDto> result = studentService.getPaging(0, 10, "id", "asc", "marcellius", "contains");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("Marcellius", result.getData().get(0).getNama());
        assertEquals(1L, result.getTotalElements());
    }

    @Test
    void testUpdateStudentSuccess() {
        UUID studentId = UUID.randomUUID();
        Student student = new Student(studentId, "Old Name", 16, "10-A", "Reguler",
                LocalDateTime.now(), LocalDateTime.now());

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentUpdateDto updateDto = new StudentUpdateDto("New Name", 17, "11-A", "Akselerasi");
        StudentResponseDto updated = studentService.update(studentId, updateDto);

        assertNotNull(updated);
        assertEquals("New Name", updated.getNama());
        assertEquals(17, updated.getUmur());
        assertEquals("11-A", updated.getKelas());
        assertEquals("Akselerasi", updated.getJenisKelas());
    }

    @Test
    void testDeleteStudentSuccess() {
        UUID studentId = UUID.randomUUID();
        when(studentRepository.existsById(studentId)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(studentId);

        assertDoesNotThrow(() -> studentService.delete(studentId));
        verify(studentRepository, times(1)).deleteById(studentId);
    }
}
