package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeChartDTO;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
            throw new IllegalStateException(LOGIN_MEMBER_REQUIRED);
        }

        List<GradeDTO> grades = gradeService.findGradesByMemberId(memberId.intValue());
        model.addAttribute("grades", grades);

        return "student/grade/gradeview";
    }

    @GetMapping("/api/grades/chart")
    @ResponseBody
    public ResponseEntity<GradeChartDTO> getGradeChartData(@RequestParam("enrollmentId") Long enrollmentId) {

        GradeChartDTO chartData = gradeService.getChartDataByEnrollmentId(enrollmentId);

        if (chartData == null) {
            chartData = GradeChartDTO.builder()
                    .attendance(0.0).assignment(0.0).exam(0.0).attitude(0.0).total(0.0)
                    .build();
        }

        return ResponseEntity.ok(chartData);
    }
}
