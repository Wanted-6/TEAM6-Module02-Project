package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/courses")
public class AdminCourseController {

    private final CourseService courseService;

    @GetMapping
    public ModelAndView findAllCourses(ModelAndView mv) {
        mv.addObject("courseList", courseService.findAllCourses());
        mv.setViewName("admin/course/list");
        return mv;
    }

    @GetMapping("/{courseId}")
    public ModelAndView findCourseById(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("course", courseService.findAdminCourseDetail(courseId));
        mv.setViewName("admin/course/detail");
        return mv;
    }

    @PostMapping("/{courseId}/approve")
    public String approveCourse(@PathVariable Integer courseId,
                                @RequestParam String adminLoginId,
                                RedirectAttributes rttr) {
        try {
            courseService.approveCourse(courseId, adminLoginId);
            rttr.addFlashAttribute("successMessage", "코스가 승인되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/reject")
    public String rejectCourse(@PathVariable Integer courseId,
                               @RequestParam String adminLoginId,
                               @RequestParam String rejectReason,
                               RedirectAttributes rttr) {
        try {
            courseService.rejectCourse(courseId, adminLoginId, rejectReason);
            rttr.addFlashAttribute("successMessage", "코스가 반려되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/delete")
    public String deleteCourse(@PathVariable Integer courseId,
                               @RequestParam String adminLoginId,
                               RedirectAttributes rttr) {
        try {
            courseService.deleteCourseByAdmin(courseId, adminLoginId);
            rttr.addFlashAttribute("successMessage", "코스가 삭제 처리되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/courses/" + courseId;
    }

    @GetMapping("/{courseId}/instructor")
    public ModelAndView findCourseInstructor(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("instructor", courseService.findInstructorByCourseId(courseId));
        mv.setViewName("admin/course/instructor");
        return mv;
    }

    @GetMapping("/{courseId}/students")
    public ModelAndView findCourseStudents(@PathVariable Integer courseId, ModelAndView mv) {
        mv.addObject("courseId", courseId);
        mv.addObject("studentList", courseService.findStudentsByCourseId(courseId));
        mv.setViewName("admin/course/students");
        return mv;
    }

}