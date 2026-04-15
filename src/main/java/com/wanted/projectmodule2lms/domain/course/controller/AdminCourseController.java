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
        mv.addObject("course", courseService.findCourseById(courseId));
        mv.setViewName("admin/course/detail");
        return mv;
    }

    @PostMapping("/{courseId}/approve")
    public String approveCourse(@PathVariable Integer courseId,
                                @RequestParam Integer adminId,
                                RedirectAttributes rttr) {
        courseService.approveCourse(courseId, adminId);
        rttr.addFlashAttribute("successMessage", "코스가 승인되었습니다.");
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/reject")
    public String rejectCourse(@PathVariable Integer courseId,
                               @RequestParam Integer adminId,
                               @RequestParam String rejectReason,
                               RedirectAttributes rttr) {
        courseService.rejectCourse(courseId, adminId, rejectReason);
        rttr.addFlashAttribute("successMessage", "코스가 반려되었습니다.");
        return "redirect:/admin/courses/" + courseId;
    }

    @PostMapping("/{courseId}/delete")
    public String deleteCourse(@PathVariable Integer courseId,
                               @RequestParam Integer adminId,
                               RedirectAttributes rttr) {
        courseService.deleteCourseByAdmin(courseId, adminId);
        rttr.addFlashAttribute("successMessage", "코스가 삭제 처리되었습니다.");
        return "redirect:/admin/courses";
    }
}