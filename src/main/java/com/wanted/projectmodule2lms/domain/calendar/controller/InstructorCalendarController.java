package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;


import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/calendar")
public class InstructorCalendarController {

    private final CalenderService calendarService;

    @GetMapping
    public String showInstructorCalendarPage(Model model) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = currentMemberId.intValue();

        model.addAttribute("events", calendarService.findInstructorCalendarEvents(instructorId));
        return "instructor/calendar/view";
    }

    @GetMapping("/events")
    @ResponseBody
    public List<CalendarEventDTO> getCalendarEvents() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = currentMemberId.intValue();

        return calendarService.findInstructorCalendarEvents(instructorId);
    }

    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@RequestParam String date) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = currentMemberId.intValue();

        return calendarService.findMemosByDate(instructorId, date);
    }

    @PostMapping
    @ResponseBody
    public String createMemo(@ModelAttribute CalendarMemoCreateDTO dto) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = currentMemberId.intValue();

        calendarService.createMemo(instructorId, dto);
        return "ok";
    }

    @PutMapping("/{memoId}")
    @ResponseBody
    public String updateMemo(@PathVariable Integer memoId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = currentMemberId.intValue();

        calendarService.updateMemo(instructorId, memoId, dto);
        return "ok";
    }

    @DeleteMapping("/{memoId}")
    @ResponseBody
    public String deleteMemo(@PathVariable Integer memoId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        Integer instructorId = currentMemberId.intValue();

        calendarService.deleteMemo(instructorId, memoId);
        return "ok";
    }
}