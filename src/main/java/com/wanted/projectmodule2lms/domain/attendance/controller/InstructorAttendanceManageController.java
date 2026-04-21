package com.wanted.projectmodule2lms.domain.attendance.controller;

import com.wanted.projectmodule2lms.domain.attendance.model.service.InstructorAttendanceService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/grades")
public class InstructorAttendanceManageController {

    private final InstructorAttendanceService instructorAttendanceService;

    @GetMapping("/manage")
    public String managePage(@RequestParam Integer courseId, Model model) {
        model.addAttribute("studentList", instructorAttendanceService.findAttendanceManageStudentsByCourse(courseId));
        model.addAttribute("courseId", courseId);
        return "instructor/grade/managepage";
    }

    @PostMapping("/manage/attendance")
    @ResponseBody
    public ResponseEntity<String> updateAttendance(@LoginMemberId Long loginMemberId,
                                                   @RequestParam Integer enrollmentId,
                                                   @RequestParam Integer sectionId,
                                                   @RequestParam String status) {
        instructorAttendanceService.updateAttendanceStatusByInstructor(
                loginMemberId.intValue(),
                enrollmentId,
                sectionId,
                status
        );
        return ResponseEntity.ok("ok");
    }
}
