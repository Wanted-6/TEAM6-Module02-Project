package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/courses")
public class StudentCourseController {

    private final CourseService courseService;

    @GetMapping
    public ModelAndView findMyCourses(@RequestParam Integer memberId, ModelAndView mv) {
        mv.addObject("memberId", memberId);
        mv.addObject("courseList", courseService.findMyCourses(memberId));
        mv.setViewName("student/course/list");
        return mv;
    }

    @GetMapping("/{courseId}")
    public ModelAndView findMyCourseDetail(@PathVariable Integer courseId,
                                           @RequestParam Integer memberId,
                                           ModelAndView mv) {
        mv.addObject("memberId", memberId);
        mv.addObject("course", courseService.findMyCourseDetail(memberId, courseId));
        mv.setViewName("student/course/detail");
        return mv;
    }
}