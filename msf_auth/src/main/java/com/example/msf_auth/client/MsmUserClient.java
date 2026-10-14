package com.example.msf_auth.client;

import com.example.library_helper.dto.ApiResponse;
import com.example.library_helper.exception.BadRequestException;
import com.example.library_helper.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * HTTP Client to communicate with msm_user microservice.
 */
@Component
public class MsmUserClient {

    private static final Logger log = LoggerFactory.getLogger(MsmUserClient.class);

    private final RestClient restClient;

    public MsmUserClient(@Value("${msm.user-service.url:http://localhost:8081}") String msmUserUrl) {
        log.info("Configuring MsmUserClient pointing to: {}", msmUserUrl);
        this.restClient = RestClient.builder()
                .baseUrl(msmUserUrl)
                .build();
    }

    public Map<String, Object> createUser(Map<String, Object> userPayload) {
        log.info("Sending create user request to msm_user: name='{}', role='{}'",
                userPayload.get("name"), userPayload.get("role"));
        try {
            ApiResponse<Map<String, Object>> response = restClient.post()
                    .uri("/api/msm_user/create/newUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(userPayload)
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});

            if (response != null && response.isSuccess()) {
                return response.getData();
            }
            throw new BadRequestException("Failed to create user in msm_user");
        } catch (Exception e) {
            log.error("Error communicating with msm_user on create: {}", e.getMessage());
            throw new BadRequestException("User creation failed: " + e.getMessage());
        }
    }

    public Map<String, Object> findUser(String loginId) {
        log.info("Looking up user '{}' from msm_user", loginId);
        try {
            ApiResponse<Map<String, Object>> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/msm_user/findUser")
                            .queryParam("loginId", loginId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<ApiResponse<Map<String, Object>>>() {});

            if (response != null && response.getData() != null) {
                return response.getData();
            }
            throw new UnauthorizedException("User not found");
        } catch (Exception e) {
            log.warn("Lookup failed for user '{}' in msm_user: {}", loginId, e.getMessage());
            throw new UnauthorizedException("Invalid login credentials");
        }
    }
}
