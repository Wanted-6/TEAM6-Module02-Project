package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;

    @GetMapping("/grades")
    public String findMyGrades(Model model) {

        Integer memberId = 1; // 임시 로그인 사용자
        List<GradeDTO> grades = gradeService.findGradesByMemberId(memberId);

        model.addAttribute("grades", grades);

        return "menu/grade";
    }
}
