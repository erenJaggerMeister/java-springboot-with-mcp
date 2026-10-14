package com.example.library_helper.exception;

import org.springframework.http.HttpStatus;

/**
 * Base custom runtime exception for all MSM microservices.
 */
public abstract class BaseException extends RuntimeException {

    private final HttpStatus status;

    public BaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public BaseException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
