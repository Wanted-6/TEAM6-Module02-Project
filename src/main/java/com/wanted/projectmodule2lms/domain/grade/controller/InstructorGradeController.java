package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeDTO;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeUpdateDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/grades")
public class InstructorGradeController {

    private final GradeService gradeService;

    // 성적 목록 조회
    @GetMapping
    public String findGradesByInstructor(Model model) {
        Integer instructorId = 12; // 임시 로그인 사용자
        model.addAttribute("grades",
                gradeService.findGradesByInstructorId(instructorId));
        return "instructor/grade/list";
    }

    // 성적 수정 페이지 이동
    @GetMapping("/edit")
    public String showEditForm(@RequestParam Integer enrollmentId, Model model) {
        Integer instructorId = 12; // 임시 로그인 사용자
        model.addAttribute("grade",
                gradeService.findGradeByEnrollmentIdForInstructor(instructorId, enrollmentId));
        return "instructor/grade/edit";
    }
    // 성적 수정 처리
    @PostMapping("/edit")
    public String updateGrade(@ModelAttribute GradeUpdateDTO dto) {
        Integer instructorId = 12; // 임시 로그인 사용자
        gradeService.updateGradeByInstructor(instructorId, dto);
        return "redirect:/instructor/grades";
    }
}