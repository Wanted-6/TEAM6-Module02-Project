package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeUpdateDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;




@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/grades")
public class InstructorGradeController {

    private final GradeService gradeService;
    private final CourseService courseService;

    // 성적 목록 조회
    @GetMapping("/courses")
    public String instructorCourseList(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }

        Integer instructorId = memberId.intValue();
        model.addAttribute("courseList", courseService.findCoursesByInstructor(instructorId));
        return "instructor/grade/course-list";
    }

    @AuditLog
    @GetMapping
    public String findGradesByInstructor(@LoginMemberId Long memberId,
                                         @RequestParam(required = false) Integer courseId,
                                         Model model) {
        if (memberId == null) {
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
        }
        Integer instructorId = memberId.intValue();

        if (courseId != null) {
            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("grades", gradeService.findGradesByInstructorIdAndCourseId(instructorId, courseId));
        } else {
            model.addAttribute("grades", gradeService.findGradesByInstructorId(instructorId));
        }
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
    // 성적 수정 처리
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
