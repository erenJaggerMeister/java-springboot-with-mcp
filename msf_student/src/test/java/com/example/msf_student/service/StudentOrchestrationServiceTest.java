package com.example.msf_student.service;

import com.example.library_helper.dto.PagingResult;
import com.example.msf_student.client.MsmStudentClient;
import com.example.msf_student.dto.StudentDto;
import com.example.msf_student.dto.StudentRequestDto;
import com.example.msf_student.service.impl.StudentOrchestrationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentOrchestrationServiceTest {

    @Mock
    private MsmStudentClient msmStudentClient;

    private StudentOrchestrationServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentOrchestrationServiceImpl(msmStudentClient);
    }

    @Test
    void testGetPagingForwardsFilterAndToken() {
        UUID studentId = UUID.randomUUID();
        StudentDto studentDto = StudentDto.builder()
                .id(studentId)
                .nama("Marcellius")
                .umur(17)
                .kelas("12-IPA-1")
                .jenisKelas("Reguler")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PagingResult<StudentDto> mockResult = PagingResult.<StudentDto>builder()
                .data(Collections.singletonList(studentDto))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .build();

        when(msmStudentClient.getPaging(0, 10, "id", "asc", "marcellius", "contains", "Bearer test-token"))
                .thenReturn(mockResult);

        PagingResult<StudentDto> result = studentService.getPaging(
                0, 10, "id", "asc", "marcellius", "contains", "Bearer test-token");

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("Marcellius", result.getData().get(0).getNama());
        assertEquals(studentId, result.getData().get(0).getId());
        verify(msmStudentClient, times(1))
                .getPaging(0, 10, "id", "asc", "marcellius", "contains", "Bearer test-token");
    }

    @Test
    void testCreateStudentSuccess() {
        StudentRequestDto request = new StudentRequestDto("Budi", 16, "10-A", "Reguler");
        UUID studentId = UUID.randomUUID();
        StudentDto studentDto = StudentDto.builder()
                .id(studentId)
                .nama("Budi")
                .umur(16)
                .kelas("10-A")
                .jenisKelas("Reguler")
                .build();

        when(msmStudentClient.create(request, "Bearer test-token")).thenReturn(studentDto);

        StudentDto result = studentService.create(request, "Bearer test-token");
        assertNotNull(result);
        assertEquals(studentId, result.getId());
        assertEquals("Budi", result.getNama());
        verify(msmStudentClient, times(1)).create(request, "Bearer test-token");
    }

    @Test
    void testGetByIdSuccess() {
        UUID studentId = UUID.randomUUID();
        StudentDto studentDto = StudentDto.builder()
                .id(studentId)
                .nama("Siti")
                .umur(17)
                .build();

        when(msmStudentClient.getById(studentId, "Bearer test-token")).thenReturn(studentDto);

        StudentDto result = studentService.getById(studentId, "Bearer test-token");
        assertNotNull(result);
        assertEquals(studentId, result.getId());
        assertEquals("Siti", result.getNama());
        verify(msmStudentClient, times(1)).getById(studentId, "Bearer test-token");
    }
}
