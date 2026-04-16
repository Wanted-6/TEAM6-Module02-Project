package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/calendar")
public class StudentCalendarController {

    private final CalenderService calendarService;

    @GetMapping
    public String showCalendarPage() {
        return "student/calendar/view";
    }

    @GetMapping("/events")
    @ResponseBody
    public List<CalendarEventDTO> getCalendarEvents() {
        Integer memberId = 1; // 임시 로그인 사용자
        return calendarService.findStudentCalendarEvents(memberId);
    }
    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@RequestParam String date) {
        Integer memberId = 1; // 임시 로그인 사용자
        return calendarService.findMemosByDate(memberId, date);
    }

    @PostMapping
    @ResponseBody
    public String createMemo(@ModelAttribute CalendarMemoCreateDTO dto) {
        Integer memberId = 1; // 임시 로그인 사용자
        calendarService.createMemo(memberId, dto);
        return "ok";
    }

    @PutMapping("/{memoId}")
    @ResponseBody
    public String updateMemo(@PathVariable Integer memoId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        Integer memberId = 1; // 임시 로그인 사용자
        calendarService.updateMemo(memberId, memoId, dto);
        return "ok";
    }

    @DeleteMapping("/{memoId}")
    @ResponseBody
    public String deleteMemo(@PathVariable Integer memoId) {
        Integer memberId = 1; // 임시 로그인 사용자
        calendarService.deleteMemo(memberId, memoId);
        return "ok";
    }
}