package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;




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
    public List<CalendarEventDTO> getCalendarEvents(@LoginMemberId Long memberId) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        return calendarService.findStudentCalendarEvents(Math.toIntExact(memberId));
    }
    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@RequestParam String date) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        return calendarService.findMemosByDate(currentMemberId.intValue(), date);
    }

    @PostMapping
    @ResponseBody
    public String createMemo(@ModelAttribute CalendarMemoCreateDTO dto) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        calendarService.createMemo(currentMemberId.intValue(), dto);
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
        calendarService.updateMemo(currentMemberId.intValue(), memoId, dto);
        return "ok";
    }

    @DeleteMapping("/{memoId}")
    @ResponseBody
    public String deleteMemo(@PathVariable Integer memoId) {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        if (currentMemberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        calendarService.deleteMemo(currentMemberId.intValue(), memoId);
        return "ok";
    }
}