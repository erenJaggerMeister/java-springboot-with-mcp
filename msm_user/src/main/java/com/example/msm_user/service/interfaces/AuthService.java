package com.example.msm_user.service.interfaces;

import com.example.msm_user.dto.LoginRequestDto;
import com.example.msm_user.dto.LoginResponseDto;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Authenticates a user and issues a Bearer JWT token signed with RSA-2048 private key.
     *
     * @param request Login credentials
     * @return Login response containing Bearer token and user info
     */
    LoginResponseDto login(LoginRequestDto request);

    /**
     * Returns the RSA-2048 Public Key in PEM format for downstream microservices.
     *
     * @return Public key PEM string
     */
    String getPublicKeyPem();
}
