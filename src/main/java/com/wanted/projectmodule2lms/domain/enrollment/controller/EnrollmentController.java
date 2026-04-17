package com.wanted.projectmodule2lms.domain.enrollment.controller;

import com.wanted.projectmodule2lms.domain.course.model.dto.CourseDTO;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.enrollment.model.dto.EnrollmentCreateDTO;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.service.EnrollmentService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private static final String ALL_CATEGORY = "\uC804\uCCB4";
    private static final String ENROLL_SUCCESS_MESSAGE = "Course enrollment completed.";

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    @AuditLog
    @GetMapping("/courses")
    public String findOpenCourses(
            @LoginMemberId Long memberId,
            @RequestParam(required = false) String category,
            Model model) {

        if (memberId == null) {
            return "redirect:/auth/login";
        }

        List<CourseDTO> courseList = courseService.findOpenCourses();
        if (category != null && !category.isBlank() && !category.equals(ALL_CATEGORY)) {
            courseList = courseList.stream()
                    .filter(course -> category.equals(course.getCategory()))
                    .toList();
        }

        Set<Integer> enrolledCourseIds = enrollmentService.findEnrollmentsByMemberId(Math.toIntExact(memberId)).stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());

        model.addAttribute("courseList", courseList);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        System.out.println("successMessage = " + model.getAttribute("successMessage"));
        System.out.println("errorMessage = " + model.getAttribute("errorMessage"));

        return "student/enrollment/list";
    }

    @AuditLog
    @GetMapping("/courses/{courseId}")
    public String findCourseDetail(
            @LoginMemberId Long memberId,
            @PathVariable Integer courseId,
            Model model) {

        if (memberId == null) {
            return "redirect:/auth/login";
        }

        CourseDTO course = courseService.findCourseById(courseId);
        boolean enrolled = enrollmentService.isAlreadyEnrolled(Math.toIntExact(memberId), courseId);

        model.addAttribute("course", course);
        model.addAttribute("enrolled", enrolled);

        return "student/enrollment/detail";
    }

    @AuditLog
    @PostMapping
    public String enrollCourse(
            @LoginMemberId Long memberId,
            @ModelAttribute EnrollmentCreateDTO request,
            RedirectAttributes rttr) {

        if (memberId == null) {
            return "redirect:/auth/login";
        }

        try {
            enrollmentService.enrollCourse(Math.toIntExact(memberId), request.getCourseId());
            rttr.addFlashAttribute("successMessage", ENROLL_SUCCESS_MESSAGE);
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/enrollments/courses";
    }
}
