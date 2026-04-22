package com.wanted.projectmodule2lms.domain.attendance.controller;

import com.wanted.projectmodule2lms.domain.attendance.model.dto.AttendanceCheckResponseDTO;
import com.wanted.projectmodule2lms.domain.attendance.model.service.AttendanceService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import com.wanted.projectmodule2lms.global.exception.UnauthorizedStudentAccessException;
import com.wanted.projectmodule2lms.global.service.CurrentMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final CurrentMemberService currentMemberService;

    @GetMapping("/{courseId}/{sectionId}")
    public String attendancePage(@PathVariable Integer courseId,
                                 @PathVariable Integer sectionId,
                                 @LoginMemberId Long loginMemberId,
                                 Model model) {
        Integer memberId = currentMemberService.toMemberId(loginMemberId);
        model.addAttribute("attendancePage", attendanceService.findAttendancePage(memberId, courseId, sectionId));
        return "student/attendance/attendancepage";
    }

    @PostMapping("/check")
    @ResponseBody
    public ResponseEntity<AttendanceCheckResponseDTO> checkAttendance(@LoginMemberId Long loginMemberId,
                                                                      @RequestParam Integer sectionId,
                                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkedAt) {
        Integer memberId = currentMemberService.toMemberId(loginMemberId);
        try {
            return ResponseEntity.ok(attendanceService.checkAttendance(memberId, sectionId, checkedAt));
        } catch (ResourceNotFoundException | UnauthorizedStudentAccessException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AttendanceCheckResponseDTO(null, e.getMessage()));
        }
    }
}