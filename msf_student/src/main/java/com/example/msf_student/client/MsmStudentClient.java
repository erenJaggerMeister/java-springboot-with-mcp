package com.example.msf_student.client;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.dto.PagingResult;
import com.example.library_helper.exception.BadRequestException;
import com.example.msf_student.dto.StudentDto;
import com.example.msf_student.dto.StudentRequestDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

/**
 * Client to communicate with msm_student microservice forwarding Bearer token for authentication and integrity.
 * Returns strongly-typed StudentDto and PagingResult<StudentDto>.
 */
@Component
public class MsmStudentClient {

    private static final Logger log = LoggerFactory.getLogger(MsmStudentClient.class);

    private final RestClient restClient;

    public MsmStudentClient(@Value("${msm.student-service.url:http://localhost:8082}") String msmStudentUrl) {
        log.info("Configuring MsmStudentClient pointing to: {}", msmStudentUrl);
        this.restClient = RestClient.builder()
                .baseUrl(msmStudentUrl)
                .build();
    }

    public PagingResult<StudentDto> getPaging(int page, int rows, String sortBy, String sortDir,
                                             String nameFilter, String matchMode, String bearerToken) {
        log.info("Calling msm_student GET /api/msm_student/getDataStudent/paging: page={}, rows={}, filter='{}'",
                page, rows, nameFilter);

        try {
            ApiResponse<PagingResult<StudentDto>> response = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/msm_student/getDataStudent/paging")
                                .queryParam("page", page)
                                .queryParam("rows", rows)
                                .queryParam("sortBy", sortBy)
                                .queryParam("sortDir", sortDir);
                        if (nameFilter != null && !nameFilter.isBlank()) {
                            uriBuilder.queryParam("filters.name.value", nameFilter);
                            uriBuilder.queryParam("filters.name.matchMode", matchMode != null ? matchMode : "contains");
                        }
                        return uriBuilder.build();
                    })
                    .header(HttpHeaders.AUTHORIZATION, bearerToken != null ? bearerToken : "")
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<PagingResult<StudentDto>>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            return PagingResult.<StudentDto>builder().build();
        } catch (Exception e) {
            log.error("Error communicating with msm_student: {}", e.getMessage());
            throw new BadRequestException("Failed to fetch students from msm_student: " + e.getMessage());
        }
    }

    public StudentDto create(StudentRequestDto payload, String bearerToken) {
        try {
            ApiResponse<StudentDto> response = restClient.post()
                    .uri("/api/msm_student/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken != null ? bearerToken : "")
                    .body(payload)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<StudentDto>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            throw new BadRequestException("Failed to create student in msm_student");
        } catch (Exception e) {
            log.error("Error creating student in msm_student: {}", e.getMessage());
            throw new BadRequestException("Student creation failed: " + e.getMessage());
        }
    }

    public StudentDto getById(UUID id, String bearerToken) {
        try {
            ApiResponse<StudentDto> response = restClient.get()
                    .uri("/api/msm_student/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken != null ? bearerToken : "")
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<StudentDto>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            throw new BadRequestException("Student not found");
        } catch (Exception e) {
            log.error("Error getting student ID {} from msm_student: {}", id, e.getMessage());
            throw new BadRequestException("Failed to get student: " + e.getMessage());
        }
    }

    public StudentDto update(UUID id, StudentRequestDto payload, String bearerToken) {
        try {
            ApiResponse<StudentDto> response = restClient.put()
                    .uri("/api/msm_student/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken != null ? bearerToken : "")
                    .body(payload)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<StudentDto>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            throw new BadRequestException("Failed to update student in msm_student");
        } catch (Exception e) {
            log.error("Error updating student ID {} in msm_student: {}", id, e.getMessage());
            throw new BadRequestException("Failed to update student: " + e.getMessage());
        }
    }

    public void delete(UUID id, String bearerToken) {
        try {
            restClient.delete()
                    .uri("/api/msm_student/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken != null ? bearerToken : "")
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Error deleting student ID {} in msm_student: {}", id, e.getMessage());
            throw new BadRequestException("Failed to delete student: " + e.getMessage());
        }
    }
}
