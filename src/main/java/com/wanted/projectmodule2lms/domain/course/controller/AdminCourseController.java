package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;

    @AuditLog
    @GetMapping
    public String findAllCourses(Model model) {
        model.addAttribute("courseList", courseService.findAdminCourseList());
        return "admin/course/list";
    }

    @AuditLog
    @GetMapping("/{courseId}")
    public String findCourseById(@PathVariable Integer courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("course", courseService.findAdminCourseDetail(courseId));
        return "admin/course/detail";
    }

    @PostMapping("/{courseId}/approve")
    public String approveCourse(@PathVariable Integer courseId,
                                Principal principal,
                                RedirectAttributes rttr) {
        try {
            courseService.approveCourse(courseId, principal.getName());
            rttr.addFlashAttribute("successMessage", "코스가 승인되었습니다.");
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/reject")
    public String rejectCourse(@PathVariable Integer courseId,
                               @RequestParam String rejectReason,
                               Principal principal,
                               RedirectAttributes rttr) {
        try {
            courseService.rejectCourse(courseId, principal.getName(), rejectReason);
            rttr.addFlashAttribute("successMessage", "코스가 반려되었습니다.");
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/delete")
    public String deleteCourse(@PathVariable Integer courseId,
                               Principal principal,
                               RedirectAttributes rttr) {
        try {
            courseService.deleteCourseByAdmin(courseId, principal.getName());
            rttr.addFlashAttribute("successMessage", "코스가 삭제 처리되었습니다.");
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @AuditLog
    @GetMapping("/{courseId}/instructor")
    public String findCourseInstructor(@PathVariable Integer courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("instructor", courseService.findInstructorByCourseId(courseId));
        return "admin/course/instructor";
    }

    @AuditLog
    @GetMapping("/{courseId}/students")
    public String findCourseStudents(@PathVariable Integer courseId, Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("studentList", courseService.findStudentsByCourseId(courseId));
        return "admin/course/students";
    }
}
