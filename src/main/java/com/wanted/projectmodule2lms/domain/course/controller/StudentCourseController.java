package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
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

    @AuditLog
    @GetMapping
    public ModelAndView findMyCourses(@LoginMemberId Long memberId,
                                      ModelAndView mv) {
        Integer studentId = requireMemberId(memberId);

        mv.addObject("memberId", memberId);
        mv.addObject("courseList", courseService.findMyCourses(studentId));
        mv.setViewName("student/course/my-classroom");
        return mv;
    }

    @GetMapping("/{courseId}")
    public ModelAndView findMyCourseDetail(@PathVariable Integer courseId,
                                           @LoginMemberId Long memberId,
                                           ModelAndView mv) {
        Integer studentId = requireMemberId(memberId);

        courseService.findMyCourseDetail(studentId, courseId);

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

    private Integer requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new LoginRequiredException("로그인한 사용자 정보가 필요합니다.");
        }
        return Math.toIntExact(memberId);
    }
}