package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;

    @AuditLog
    @GetMapping
    public ModelAndView findAllCourses(ModelAndView mv) {
        mv.addObject("courseList", courseService.findAdminCourseList());
        mv.setViewName("admin/course/list");
        return mv;
    }

    @AuditLog
    @GetMapping("/{courseId}")
    public ModelAndView findCourseById(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findAdminCourseDetail(courseId));
        mv.setViewName("admin/course/detail");
        return mv;
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
    public ModelAndView findCourseInstructor(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("instructor", courseService.findInstructorByCourseId(courseId));
        mv.setViewName("admin/course/instructor");
        return mv;
    }

    @AuditLog
    @GetMapping("/{courseId}/students")
    public ModelAndView findCourseStudents(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("studentList", courseService.findStudentsByCourseId(courseId));
        mv.setViewName("admin/course/students");
        return mv;
    }
}