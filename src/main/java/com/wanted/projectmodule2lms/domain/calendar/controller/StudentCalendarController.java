package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/calendar")
public class StudentCalendarController {

    private static final String LOGIN_MEMBER_REQUIRED = "로그인한 사용자 정보가 필요합니다.";

    private final CalenderService calendarService;

    @AuditLog
    @GetMapping
    public String showCalendarPage() {
        return "student/calendar/view";
    }

    @AuditLog
    @GetMapping("/events")
    @ResponseBody
    public List<CalendarEventDTO> getCalendarEvents(@LoginMemberId Long memberId) {
        if (memberId == null) {
            throw new IllegalStateException(LOGIN_MEMBER_REQUIRED);
        }
        return calendarService.findStudentCalendarEvents(memberId.intValue());
    }

    @AuditLog
    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@LoginMemberId Long memberId,
                                  @RequestParam String date) {
        if (memberId == null) {
            throw new IllegalStateException(LOGIN_MEMBER_REQUIRED);
        }
        return calendarService.findMemosByDate(memberId.intValue(), date);
    }

    @AuditLog
    @PostMapping
    @ResponseBody
    public String createMemo(@LoginMemberId Long memberId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        if (memberId == null) {
            throw new IllegalStateException(LOGIN_MEMBER_REQUIRED);
        }
        calendarService.createMemo(memberId.intValue(), dto);
        return "ok";
    }

    @AuditLog
    @PutMapping("/{memoId}")
    @ResponseBody
    public String updateMemo(@LoginMemberId Long memberId,
                             @PathVariable Integer memoId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        if (memberId == null) {
            throw new IllegalStateException(LOGIN_MEMBER_REQUIRED);
        }
        calendarService.updateMemo(memberId.intValue(), memoId, dto);
        return "ok";
    }

    @AuditLog
    @DeleteMapping("/{memoId}")
    @ResponseBody
    public String deleteMemo(@LoginMemberId Long memberId,
                             @PathVariable Integer memoId) {
        if (memberId == null) {
            throw new IllegalStateException(LOGIN_MEMBER_REQUIRED);
        }
        calendarService.deleteMemo(memberId.intValue(), memoId);
        return "ok";
    }
}
