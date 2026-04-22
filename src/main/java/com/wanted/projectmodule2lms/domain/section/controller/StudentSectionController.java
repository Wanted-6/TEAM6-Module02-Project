package com.wanted.projectmodule2lms.domain.section.controller;

import com.wanted.projectmodule2lms.domain.section.service.SectionService;
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
@RequestMapping("/student/courses/{courseId}/sections")
public class StudentSectionController {

    private final SectionService sectionService;

    @AuditLog
    @GetMapping
    public String findMySections(@PathVariable Integer courseId,
                                 @LoginMemberId Long memberId,
                                 Model model) {
        Integer studentId = requireMemberId(memberId);

        model.addAttribute("memberId", memberId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("sectionList", sectionService.findMySections(studentId, courseId));
        return "student/section/list";
    }

    @AuditLog
    @GetMapping("/{sectionId}")
    public String findMySectionDetail(@PathVariable Integer courseId,
                                      @PathVariable Integer sectionId,
                                      @LoginMemberId Long memberId,
                                      Model model) {
        Integer studentId = requireMemberId(memberId);

        model.addAttribute("memberId", memberId);
        model.addAttribute("courseId", courseId);
        model.addAttribute("section", sectionService.findMySectionDetail(studentId, courseId, sectionId));
        return "student/section/detail";
    }

    private Integer requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new LoginRequiredException("로그인한 사용자 정보가 필요합니다.");
        }
        return Math.toIntExact(memberId);
    }
}
