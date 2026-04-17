package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/calendar")
public class StudentCalendarController {

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
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
        }
        return calendarService.findStudentCalendarEvents(memberId.intValue());
    }

    @AuditLog
    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@LoginMemberId Long memberId,
                                  @RequestParam String date) {
        if (memberId == null) {
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
        }
        return calendarService.findMemosByDate(memberId.intValue(), date);
    }

    @AuditLog
    @PostMapping
    @ResponseBody
    public String createMemo(@LoginMemberId Long memberId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        if (memberId == null) {
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
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
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
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
            throw new IllegalStateException("�α��� ����� ������ ã�� �� �����ϴ�.");
        }
        calendarService.deleteMemo(memberId.intValue(), memoId);
        return "ok";
    }
}
