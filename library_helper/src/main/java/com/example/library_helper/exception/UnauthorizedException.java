package com.example.library_helper.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication or authorization fails (HTTP 401).
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
