package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class GradeController {

    private static final String LOGIN_MEMBER_REQUIRED = "Login member id is required.";

    private final GradeService gradeService;

    @AuditLog
    @GetMapping("/grades")
    public String findMyGrades(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
<<<<<<< HEAD
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
=======
            throw new IllegalStateException(LOGIN_MEMBER_REQUIRED);
>>>>>>> 6c01c8db3e1688f341c4d9b2d0ffcaaf727f223d
        }

        List<GradeDTO> grades = gradeService.findGradesByMemberId(memberId.intValue());
        model.addAttribute("grades", grades);

        return "student/grade/gradeview";
    }
}
