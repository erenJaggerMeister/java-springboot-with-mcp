package com.example.msf_auth.service.interfaces;

import com.example.msf_auth.dto.LoginRequestDto;
import com.example.msf_auth.dto.LoginResponseDto;
import com.example.msf_auth.dto.SignUpRequestDto;

import java.util.Map;

/**
 * Service interface for authentication and registration in msf_auth.
 */
public interface AuthService {

    /**
     * Signs up a new user: hashes the password in msf_auth and passes to msm_user.
     */
    Map<String, Object> signUp(SignUpRequestDto request);

    /**
     * Authenticates user against msm_user and issues a signed Bearer token with RSA-2048 private key.
     */
    LoginResponseDto login(LoginRequestDto request);

    /**
     * Returns the RSA-2048 Public Key in PEM format.
     */
    String getPublicKeyPem();
}
