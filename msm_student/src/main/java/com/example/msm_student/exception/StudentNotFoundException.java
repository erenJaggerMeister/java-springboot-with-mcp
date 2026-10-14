package com.example.msm_student.exception;

import com.example.library_helper.exception.ResourceNotFoundException;

import java.util.UUID;

/**
 * Exception thrown when a Student is not found.
 */
public class StudentNotFoundException extends ResourceNotFoundException {

    public StudentNotFoundException(UUID id) {
        super("Student", "id", id);
    }
}
