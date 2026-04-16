package com.wanted.projectmodule2lms.domain.attendance.controller;

import com.wanted.projectmodule2lms.domain.attendance.model.dto.AttendanceCheckResponseDTO;
import com.wanted.projectmodule2lms.domain.attendance.model.service.AttendanceService;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/{courseId}/{sectionId}")
    public ModelAndView attendancePage(@PathVariable Integer courseId,
                                       @PathVariable Integer sectionId,
                                       ModelAndView mv) {
        Integer memberId = getCurrentMemberId();
        mv.addObject("attendancePage", attendanceService.findAttendancePage(memberId, courseId, sectionId));
        mv.setViewName("student/attendance/attendancepage");
        return mv;
    }

    @PostMapping("/check")
    @ResponseBody
    public ResponseEntity<AttendanceCheckResponseDTO> checkAttendance(@RequestParam Integer sectionId,
                                                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime checkedAt) {
        Integer memberId = getCurrentMemberId();
        try {
            return ResponseEntity.ok(attendanceService.checkAttendance(memberId, sectionId, checkedAt));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AttendanceCheckResponseDTO(null, e.getMessage()));
        }
    }

    private Integer getCurrentMemberId() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return currentMemberId != null ? currentMemberId.intValue() : null;
    }
}
