package com.wanted.projectmodule2lms.domain.enrollment.controller;

import com.wanted.projectmodule2lms.domain.course.model.dto.CourseDTO;
import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.enrollment.model.dto.EnrollmentCreateDTO;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;


@Controller
@RequestMapping("/student/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    @GetMapping("/courses")
    public String findOpenCourses(@RequestParam(required = false) String category,
                                  Model model) {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer memberId = currentMemberId.intValue();

        List<CourseDTO> courseList = courseService.findOpenCourses();
        if (category != null && !category.isBlank() && !category.equals("전체")) {
            courseList = courseList.stream()
                    .filter(course -> category.equals(course.getCategory()))
                    .toList();
        }

        Set<Integer> enrolledCourseIds = enrollmentService.findEnrollmentsByMemberId(memberId).stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());

        model.addAttribute("courseList", courseList);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);

        System.out.println("successMessage = " + model.getAttribute("successMessage"));
        System.out.println("errorMessage = " + model.getAttribute("errorMessage"));

        return "student/enrollment/list";
    }

    @GetMapping("/courses/{courseId}")
    public String findCourseDetail(@PathVariable Integer courseId,
                                   Model model) {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer memberId = currentMemberId.intValue();

        CourseDTO course = courseService.findCourseById(courseId);
        boolean enrolled = enrollmentService.isAlreadyEnrolled(memberId, courseId);

        model.addAttribute("course", course);
        model.addAttribute("enrolled", enrolled);

        return "student/enrollment/detail";
    }

    @PostMapping
    public String enrollCourse(@ModelAttribute EnrollmentCreateDTO request,
                               RedirectAttributes rttr) {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer memberId = currentMemberId.intValue();

        try {
            enrollmentService.enrollCourse(memberId, request.getCourseId());
            rttr.addFlashAttribute("successMessage", "수강신청이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/student/enrollments/courses";
    }
}