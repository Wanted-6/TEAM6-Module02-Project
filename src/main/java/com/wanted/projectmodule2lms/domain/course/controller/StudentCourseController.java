package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/courses")
public class StudentCourseController {

    private final CourseService courseService;

    @GetMapping
    public ModelAndView findMyCourses(
            @LoginMemberId Long memberId,
            ModelAndView mv) {

        // 로그인 체크 (2중)
        if (memberId == null) {
            mv.setViewName("redirect:/auth/login");
            return mv;
        }

        mv.addObject("memberId", memberId);
        mv.addObject("courseList", courseService.findMyCourses(Math.toIntExact(memberId)));
        mv.setViewName("student/course/list");
        return mv;
    }

    @GetMapping("/{courseId}")
    public ModelAndView findMyCourseDetail(
            @PathVariable Integer courseId,
            @LoginMemberId Long memberId,
            ModelAndView mv) {

        if (memberId == null) {
            mv.setViewName("redirect:/auth/login");
            return mv;
        }

        mv.addObject("memberId", memberId);
        mv.addObject("course", courseService.findMyCourseDetail(Math.toIntExact(memberId), courseId));
        mv.setViewName("student/course/detail");
        return mv;
    }
}