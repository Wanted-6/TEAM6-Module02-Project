package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
}