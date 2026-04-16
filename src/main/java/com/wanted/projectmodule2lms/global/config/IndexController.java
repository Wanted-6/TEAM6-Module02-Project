package com.wanted.projectmodule2lms.global.config;

import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;

    @GetMapping(value = {"/", "/main"})
    public String mainPage(Model model) {
        model.addAttribute("myClassroomUrl", findMyClassroomUrl());
        return "main/main";
    }

    private String findMyClassroomUrl() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();

        if (currentMemberId == null) {
            return "/auth/login";
        }

        List<Enrollment> enrollmentList = enrollmentRepository.findByMemberId(currentMemberId.intValue());

        if (enrollmentList.isEmpty()) {
            return "/main";
        }

        Enrollment enrollment = enrollmentList.get(0);
        List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(enrollment.getCourseId());

        if (sectionList.isEmpty()) {
            return "/main";
        }

        Section section = sectionList.get(0);
        return "/student/attendance/" + enrollment.getCourseId() + "/" + section.getSectionId();
    }
}
