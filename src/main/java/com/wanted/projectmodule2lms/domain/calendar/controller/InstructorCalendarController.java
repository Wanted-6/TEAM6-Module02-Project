package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
}