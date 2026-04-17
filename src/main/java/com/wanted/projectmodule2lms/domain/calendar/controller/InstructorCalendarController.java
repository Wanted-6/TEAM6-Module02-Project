package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/calendar")
public class InstructorCalendarController {

    private final CalenderService calendarService;

    @AuditLog
    @GetMapping
    public String showInstructorCalendarPage(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = memberId.intValue();

        model.addAttribute("events", calendarService.findInstructorCalendarEvents(instructorId));
        return "instructor/calendar/view";
    }

    @AuditLog
    @GetMapping("/events")
    @ResponseBody
    public List<CalendarEventDTO> getCalendarEvents(@LoginMemberId Long memberId) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = memberId.intValue();

        return calendarService.findInstructorCalendarEvents(instructorId);
    }

    @AuditLog
    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@LoginMemberId Long memberId,
                                  @RequestParam String date) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = memberId.intValue();

        return calendarService.findMemosByDate(instructorId, date);
    }

    @PostMapping
    @ResponseBody
    public String createMemo(@LoginMemberId Long memberId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = memberId.intValue();

        calendarService.createMemo(instructorId, dto);
        return "ok";
    }

    @PutMapping("/{memoId}")
    @ResponseBody
    public String updateMemo(@LoginMemberId Long memberId,
                             @PathVariable Integer memoId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = memberId.intValue();

        calendarService.updateMemo(instructorId, memoId, dto);
        return "ok";
    }

    @DeleteMapping("/{memoId}")
    @ResponseBody
    public String deleteMemo(@LoginMemberId Long memberId,
                             @PathVariable Integer memoId) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = memberId.intValue();

        calendarService.deleteMemo(instructorId, memoId);
        return "ok";
    }
}