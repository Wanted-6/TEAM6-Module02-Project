package com.wanted.projectmodule2lms.domain.course.controller;

import com.wanted.projectmodule2lms.domain.course.model.service.CourseService;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/courses")
public class StudentCourseController {

    private final CourseService courseService;
    private final SectionRepository sectionRepository;

    @AuditLog
    @GetMapping
    public String findMyCourses(@LoginMemberId Long memberId,
                                Model model) {
        Integer studentId = requireMemberId(memberId);

        model.addAttribute("courseList", courseService.findMyCourses(studentId));
        return "student/course/my-classroom";
    }

    @GetMapping("/{courseId}")
    public String findMyCourseDetail(@PathVariable Integer courseId,
                                     @LoginMemberId Long memberId) {
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
            return "redirect:/student/courses";
        }

        return "redirect:/student/attendance/" + courseId + "/" + section.getSectionId();
    }

    private Integer requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new LoginRequiredException("로그인한 사용자 정보가 필요합니다.");
        }
        return Math.toIntExact(memberId);
    }
}
