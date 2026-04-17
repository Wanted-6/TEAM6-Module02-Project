package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
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
    private final SectionRepository sectionRepository;

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
        mv.setViewName("student/course/my-classroom");
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

        courseService.findMyCourseDetail(Math.toIntExact(memberId), courseId);

        Section section = sectionRepository.findByCourseIdAndSectionOrder(courseId, 1)
                .orElseGet(() -> {
                    java.util.List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);
                    if (sectionList.isEmpty()) {
                        return null;
                    }
                    return sectionList.get(0);
                });

        if (section == null) {
            mv.setViewName("redirect:/student/courses");
            return mv;
        }

        mv.setViewName("redirect:/student/attendance/" + courseId + "/" + section.getSectionId());
        return mv;
    }
}
