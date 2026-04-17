package com.wanted.projectmodule2lms.domain.instructorgrade.controller;

import com.wanted.projectmodule2lms.domain.instructorgrade.model.service.InstructorGradeService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/grades")
public class InstructorGradeManageController {
    private final InstructorGradeService instructorGradeService;

    @GetMapping("/manage")
    public String managePage(@RequestParam Integer courseId, Model model) {
     model.addAttribute("studentList",instructorGradeService.findStudentsByCourse(courseId));
     model.addAttribute("courseId", courseId);
        return "instructor/grade/managepage";
    }

    @AuditLog
    @PostMapping("/manage/attendance")
    @ResponseBody
    public ResponseEntity<String> updateAttendance(@LoginMemberId Long loginMemberId,
                                                   @RequestParam Integer enrollmentId,
                                                   @RequestParam Integer sectionId,
                                                   @RequestParam String status) {
        instructorGradeService.updateAttendanceStatus(loginMemberId.intValue(), enrollmentId, sectionId, status);
        return ResponseEntity.ok("ok");
    }
}
