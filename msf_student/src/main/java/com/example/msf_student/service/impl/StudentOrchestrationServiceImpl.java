package com.example.msf_student.service.impl;

import com.example.library_helper.dto.PagingResult;
import com.example.msf_student.client.MsmStudentClient;
import com.example.msf_student.dto.StudentDto;
import com.example.msf_student.dto.StudentRequestDto;
import com.example.msf_student.service.interfaces.StudentOrchestrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service implementation for Student Orchestration in msf_student.
 * Enforces authentication and token forwarding to msm_student.
 * Returns strongly-typed StudentDto and PagingResult<StudentDto>.
 */
@Service
public class StudentOrchestrationServiceImpl implements StudentOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(StudentOrchestrationServiceImpl.class);

    private final MsmStudentClient msmStudentClient;

    public StudentOrchestrationServiceImpl(MsmStudentClient msmStudentClient) {
        this.msmStudentClient = msmStudentClient;
    }

    @Override
    public PagingResult<StudentDto> getPaging(int page, int rows, String sortBy, String sortDir,
                                             String nameFilter, String matchMode, String bearerToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentCaller = auth != null ? auth.getName() : "ANONYMOUS";

        log.info("[MSF-STUDENT-PAGING] User '{}' requesting paging from msm_student: page={}, rows={}, filter='{}', matchMode='{}'",
                currentCaller, page, rows, nameFilter, matchMode);

        return msmStudentClient.getPaging(page, rows, sortBy, sortDir, nameFilter, matchMode, bearerToken);
    }

    @Override
    public StudentDto create(StudentRequestDto payload, String bearerToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentCaller = auth != null ? auth.getName() : "ANONYMOUS";

        log.info("[MSF-STUDENT-CREATE] User '{}' requesting create student in msm_student: nama='{}'",
                currentCaller, payload.getNama());

        return msmStudentClient.create(payload, bearerToken);
    }

    @Override
    public StudentDto getById(UUID id, String bearerToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentCaller = auth != null ? auth.getName() : "ANONYMOUS";

        log.info("[MSF-STUDENT-GET] User '{}' requesting get student ID: {}", currentCaller, id);
        return msmStudentClient.getById(id, bearerToken);
    }

    @Override
    public StudentDto update(UUID id, StudentRequestDto payload, String bearerToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentCaller = auth != null ? auth.getName() : "ANONYMOUS";

        log.info("[MSF-STUDENT-UPDATE] User '{}' requesting update student ID: {}", currentCaller, id);
        return msmStudentClient.update(id, payload, bearerToken);
    }

    @Override
    public void delete(UUID id, String bearerToken) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentCaller = auth != null ? auth.getName() : "ANONYMOUS";

        log.info("[MSF-STUDENT-DELETE] User '{}' requesting delete student ID: {}", currentCaller, id);
        msmStudentClient.delete(id, bearerToken);
    }
}
