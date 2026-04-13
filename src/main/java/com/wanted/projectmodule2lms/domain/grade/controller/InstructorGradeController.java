package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/grades")
public class InstructorGradeController {

    private final GradeService gradeService;

    @GetMapping
    public String findGradesByInstructor(Model model) {
        Integer instructorId = 12; // 로그인 가정
        List<GradeDTO> grades = gradeService.findGradesByInstructorId(instructorId);
        model.addAttribute("grades", grades);
        return "instructor/grade/list";
    }
}
