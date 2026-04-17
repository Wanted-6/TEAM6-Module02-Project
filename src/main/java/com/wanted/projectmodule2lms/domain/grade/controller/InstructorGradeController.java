package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeUpdateDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/grades")
public class InstructorGradeController {

    private final GradeService gradeService;

    @AuditLog
    @GetMapping
    public String findGradesByInstructor(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
        }
        Integer instructorId = memberId.intValue();

        model.addAttribute("grades", gradeService.findGradesByInstructorId(instructorId));
        return "instructor/grade/list-view";
    }


    @GetMapping("/edit")
    public String showEditForm(@LoginMemberId Long memberId,
                               @RequestParam Integer enrollmentId,
                               Model model) {
        if (memberId == null) {
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
        }
        Integer instructorId = memberId.intValue();

        model.addAttribute("grade",
                gradeService.findGradeByEnrollmentIdForInstructor(instructorId, enrollmentId));
        return "instructor/grade/edit";
    }


    @PostMapping("/edit")
    public String updateGrade(@LoginMemberId Long memberId,
                              @ModelAttribute GradeUpdateDTO dto) {
        if (memberId == null) {
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
        }
        Integer instructorId = memberId.intValue();

        gradeService.updateGradeByInstructor(instructorId, dto);
        return "redirect:/instructor/grades";
    }
}
