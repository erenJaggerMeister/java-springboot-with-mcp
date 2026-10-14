package com.example.library_helper.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when input data or request is invalid (HTTP 400).
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
