package com.wanted.projectmodule2lms.global.exception;

public class UnauthorizedStudentAccessException extends RuntimeException {
    public UnauthorizedStudentAccessException(String message) {
        super(message);
    }
}

