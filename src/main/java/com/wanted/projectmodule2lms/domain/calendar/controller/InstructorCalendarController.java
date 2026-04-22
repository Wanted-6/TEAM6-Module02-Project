package com.wanted.projectmodule2lms.domain.calendar.controller;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.service.CalenderService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
@RequestMapping("/instructor/calendar")
public class InstructorCalendarController {

    private static final String LOGIN_MEMBER_REQUIRED = "로그인한 사용자 정보가 필요합니다.";

    private final CalenderService calendarService;

    @AuditLog
    @GetMapping
    public String showInstructorCalendarPage(@LoginMemberId Long memberId, Model model) {
        Integer instructorId = requireMemberId(memberId);

        model.addAttribute("events", calendarService.findInstructorCalendarEvents(instructorId));
        return "instructor/calendar/view";
    }

    @AuditLog
    @GetMapping("/events")
    @ResponseBody
    public ResponseEntity<?> getCalendarEvents(@LoginMemberId Long memberId) {
        try {
            return ResponseEntity.ok(calendarService.findInstructorCalendarEvents(requireMemberId(memberId)));
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @AuditLog
    @GetMapping("/memos")
    @ResponseBody
    public ResponseEntity<?> getMemosByDate(@LoginMemberId Long memberId,
                                            @RequestParam String date) {
        try {
            return ResponseEntity.ok(calendarService.findMemosByDate(requireMemberId(memberId), date));
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }


    @PostMapping
    @ResponseBody
    public ResponseEntity<String> createMemo(@LoginMemberId Long memberId,
                                             @ModelAttribute CalendarMemoCreateDTO dto) {
        try {
            calendarService.createMemo(requireMemberId(memberId), dto);
            return ResponseEntity.ok("ok");
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }


    @PutMapping("/{memoId}")
    @ResponseBody
    public ResponseEntity<String> updateMemo(@LoginMemberId Long memberId,
                                             @PathVariable Integer memoId,
                                             @ModelAttribute CalendarMemoCreateDTO dto) {
        try {
            calendarService.updateMemo(requireMemberId(memberId), memoId, dto);
            return ResponseEntity.ok("ok");
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @DeleteMapping("/{memoId}")
    @ResponseBody
    public ResponseEntity<String> deleteMemo(@LoginMemberId Long memberId,
                                             @PathVariable Integer memoId) {
        try {
            calendarService.deleteMemo(requireMemberId(memberId), memoId);
            return ResponseEntity.ok("ok");
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private Integer requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new LoginRequiredException(LOGIN_MEMBER_REQUIRED);
        }
        return memberId.intValue();
    }
}
