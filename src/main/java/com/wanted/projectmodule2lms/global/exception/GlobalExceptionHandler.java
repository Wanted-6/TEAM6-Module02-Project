package com.wanted.projectmodule2lms.global.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/error";
    }

    @ExceptionHandler(UnauthorizedInstructorException.class)
    public String handleUnauthorized(UnauthorizedInstructorException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error/403";
    }
}
