package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;


@Controller
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/grades")
    public String findMyGrades(Model model) {

        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer memberId = currentMemberId.intValue();
        List<GradeDTO> grades = gradeService.findGradesByMemberId(memberId);

        model.addAttribute("grades", grades);

        return "student/grade/gradeview";
    }
}
