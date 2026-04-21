package com.wanted.projectmodule2lms.domain.enrollment.controller;

import com.wanted.projectmodule2lms.domain.enrollment.exception.EnrollmentException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(assignableTypes = EnrollmentController.class)
public class EnrollmentExceptionHandler {

    @ExceptionHandler(EnrollmentException.class)
    public String handleEnrollmentException(EnrollmentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/student/enrollments/courses";
    }
}
