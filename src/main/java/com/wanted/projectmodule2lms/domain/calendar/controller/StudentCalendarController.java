package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        return calendarService.findStudentCalendarEvents(memberId.intValue());
    }

    @GetMapping("/memos")
    @ResponseBody
    public List<?> getMemosByDate(@LoginMemberId Long memberId,
                                  @RequestParam String date) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        return calendarService.findMemosByDate(memberId.intValue(), date);
    }


    @PostMapping
    @ResponseBody
    public String createMemo(@LoginMemberId Long memberId,
                             @ModelAttribute CalendarMemoCreateDTO dto) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        calendarService.createMemo(memberId.intValue(), dto);
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
        calendarService.updateMemo(memberId.intValue(), memoId, dto);
        return "ok";
    }


    @DeleteMapping("/{memoId}")
    @ResponseBody
    public String deleteMemo(@LoginMemberId Long memberId,
                             @PathVariable Integer memoId) {
        if (memberId == null) {
            throw new IllegalStateException("로그인 사용자 정보를 찾을 수 없습니다.");
        }
        calendarService.deleteMemo(memberId.intValue(), memoId);
        return "ok";
    }

}