package com.wanted.projectmodule2lms.domain.grade.controller;

import com.wanted.projectmodule2lms.domain.course.model.service.CourseService;
import com.wanted.projectmodule2lms.domain.grade.model.dto.GradeUpdateDTO;
import com.wanted.projectmodule2lms.domain.grade.model.service.GradeService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/grades")
public class InstructorGradeController {

    private static final String LOGIN_MEMBER_REQUIRED = "로그인한 사용자 정보가 필요합니다.";

    private final GradeService gradeService;
    private final CourseService courseService;

    @GetMapping("/courses")
    public String instructorCourseList(@LoginMemberId Long memberId, Model model) {


        Integer instructorId = memberId.intValue();
        model.addAttribute("courseList", courseService.findCoursesByInstructor(instructorId));
        return "instructor/grade/course-list";
    }

    @AuditLog
    @GetMapping
    public String findGradesByInstructor(@LoginMemberId Long memberId,
                                         @RequestParam(required = false) Integer courseId,
                                         Model model) {

        Integer instructorId = memberId.intValue();

        if (courseId != null) {
            model.addAttribute("selectedCourseId", courseId);
            model.addAttribute("dashboard",gradeService.findDashboardSummaryByCourseId(instructorId, courseId));
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

        Integer instructorId = memberId.intValue();

        model.addAttribute("grade",
                gradeService.findGradeByEnrollmentIdForInstructor(instructorId, enrollmentId));
        return "instructor/grade/edit";
    }

    @PostMapping("/edit")
    public String updateGrade(@LoginMemberId Long memberId,
                              @ModelAttribute GradeUpdateDTO dto) {

        Integer instructorId = memberId.intValue();

        gradeService.updateGradeByInstructor(instructorId, dto);
        return "redirect:/instructor/grades";
    }
}
