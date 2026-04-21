package com.wanted.projectmodule2lms.global.exception;

public class LoginRequiredException extends RuntimeException {
    public LoginRequiredException(String message) {
        super(message);
    }
}
