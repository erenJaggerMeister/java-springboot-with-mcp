package com.example.msf_user.client;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.dto.PagingResult;
import com.example.library_helper.exception.BadRequestException;
import com.example.msf_user.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Client to communicate with msm_user microservice returning strongly-typed UserDto objects.
 */
@Component
public class MsmUserClient {

    private static final Logger log = LoggerFactory.getLogger(MsmUserClient.class);

    private final RestClient restClient;

    public MsmUserClient(@Value("${msm.user-service.url:http://localhost:8081}") String msmUserUrl) {
        log.info("Configuring MsmUserClient in msf_user pointing to: {}", msmUserUrl);
        this.restClient = RestClient.builder()
                .baseUrl(msmUserUrl)
                .build();
    }

    public UserDto createUser(Map<String, Object> userPayload, String bearerToken) {
        log.info("Calling msm_user POST /api/msm_user/create/newUser: {}", userPayload.get("name"));
        try {
            ApiResponse<UserDto> response = restClient.post()
                    .uri("/api/msm_user/create/newUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken != null ? bearerToken : "")
                    .body(userPayload)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<UserDto>>() {});

            if (response != null && response.isSuccess() && response.getData() != null) {
                return response.getData();
            }
            throw new BadRequestException("Failed to save user in msm_user");
        } catch (Exception e) {
            log.error("Error creating user in msm_user: {}", e.getMessage());
            throw new BadRequestException("Error creating user: " + e.getMessage());
        }
    }

    public PagingResult<UserDto> getPaging(int page, int rows, String sortBy, String sortDir, String bearerToken) {
        log.info("Calling msm_user GET /api/msm_user/getDataUser/paging: page={}, rows={}", page, rows);
        try {
            ApiResponse<PagingResult<UserDto>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/msm_user/getDataUser/paging")
                            .queryParam("page", page)
                            .queryParam("rows", rows)
                            .queryParam("sortBy", sortBy)
                            .queryParam("sortDir", sortDir)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, bearerToken != null ? bearerToken : "")
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<PagingResult<UserDto>>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            return PagingResult.<UserDto>builder().build();
        } catch (Exception e) {
            log.error("Error fetching user paging from msm_user: {}", e.getMessage());
            throw new BadRequestException("Error fetching users: " + e.getMessage());
        }
    }
}
