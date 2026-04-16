package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/instructor/calendar")
public class InstructorCalendarController {

    private final CalenderService calendarService;

    @GetMapping
    public String showInstructorCalendarPage(Model model) {
        Integer instructorId = 11; // 임시 로그인 강사

        model.addAttribute("events", calendarService.findInstructorCalendarEvents(instructorId));

        return "instructor/calendar/view";
    }
    @GetMapping("/events")
    @ResponseBody
    public List<CalendarEventDTO> getCalendarEvents() {
        Integer instructorId = 11; // 임시 로그인 강사
        return calendarService.findInstructorCalendarEvents(instructorId);
    }

    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@RequestParam String date) {
        Integer instructorId = 11; // 임시 로그인 강사
        return calendarService.findMemosByDate(instructorId, date);
    }

    @PostMapping
    @ResponseBody
    public String createMemo(@ModelAttribute CalendarMemoCreateDTO dto) {
        Integer instructorId = 11; // 임시 로그인 강사
        calendarService.createMemo(instructorId, dto);
        return "ok";
    }
}