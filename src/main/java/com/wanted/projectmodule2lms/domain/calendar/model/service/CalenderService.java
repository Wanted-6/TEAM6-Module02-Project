package com.wanted.projectmodule2lms.domain.calendar.model.service;

import com.wanted.projectmodule2lms.domain.calendar.model.dao.CalendarMemoRepository;
import com.wanted.projectmodule2lms.domain.calendar.model.dao.CalendarQueryRepository;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.entity.CalendarMemo;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalenderService {

    private final CalendarQueryRepository calendarQueryRepository;
    private final CalendarMemoRepository calendarMemoRepository;

    public List<CalendarEventDTO> findStudentCalendarEvents(Integer memberId) {
        List<CalendarEventDTO> result = new ArrayList<>();

        result.addAll(calendarQueryRepository.findStudentSectionEvents(memberId));
        result.addAll(calendarQueryRepository.findStudentAssignmentEvents(memberId));
        result.addAll(calendarQueryRepository.findStudentExamEvents(memberId));

        List<CalendarEventDTO> memoEvents = calendarMemoRepository.findByMemberId(memberId)
                .stream()
                .map(memo -> new CalendarEventDTO(
                        "memo-" + memo.getMemoId(),
                        "[메모] " + memo.getContent(),
                        memo.getMemoDate().toString(),
                        getMemoColor(memo.getMemoDate()),
                        null
                ))
                .toList();

        result.addAll(memoEvents);
        return result;
    }

    public List<CalendarEventDTO> findInstructorCalendarEvents(Integer instructorId) {
        List<CalendarEventDTO> result = new ArrayList<>();

        result.addAll(calendarQueryRepository.findInstructorSectionEvents(instructorId));
        result.addAll(calendarQueryRepository.findInstructorAssignmentEvents(instructorId));
        result.addAll(calendarQueryRepository.findInstructorExamEvents(instructorId));

        List<CalendarEventDTO> memoEvents = calendarMemoRepository.findByMemberId(instructorId)
                .stream()
                .map(memo -> new CalendarEventDTO(
                        "memo-" + memo.getMemoId(),
                        "[메모] " + memo.getContent(),
                        memo.getMemoDate().toString(),
                        getMemoColor(memo.getMemoDate()),
                        null
                ))
                .toList();

        result.addAll(memoEvents);
        return result;
    }

    public List<CalendarMemoDTO> findMemosByDate(Integer memberId, String date) {
        return calendarMemoRepository.findByMemberIdAndMemoDate(memberId, LocalDate.parse(date))
                .stream()
                .map(memo -> new CalendarMemoDTO(
                        memo.getMemoId(),
                        memo.getContent(),
                        memo.getMemoDate().toString()
                ))
                .toList();
    }

    public void createMemo(Integer memberId, CalendarMemoCreateDTO dto) {
        CalendarMemo memo = new CalendarMemo(
                memberId,
                dto.getContent(),
                LocalDate.parse(dto.getMemoDate())
        );

        calendarMemoRepository.save(memo);
    }

    public void updateMemo(Integer memberId, Integer memoId, CalendarMemoCreateDTO dto) {
        CalendarMemo memo = calendarMemoRepository.findById(memoId)
                .orElseThrow(() -> new ResourceNotFoundException("메모를 찾을 수 없습니다."));

        if (!memo.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인 메모만 수정할 수 있습니다.");
        }

        memo.changeContent(dto.getContent());
    }

    public void deleteMemo(Integer memberId, Integer memoId) {
        CalendarMemo memo = calendarMemoRepository.findById(memoId)
                .orElseThrow(() -> new ResourceNotFoundException("메모를 찾을 수 없습니다."));

        if (!memo.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인 메모만 삭제할 수 있습니다.");
        }

        calendarMemoRepository.delete(memo);
    }

    private String getMemoColor(LocalDate memoDate) {
        LocalDate today = LocalDate.now();
        long diffDays = java.time.temporal.ChronoUnit.DAYS.between(today, memoDate);

        if (diffDays < 0) {
            return "#9ca3af";
        }
        if (diffDays == 3) {
            return "#facc15";
        }
        if (diffDays == 2) {
            return "#fb923c";
        }
        if (diffDays == 1 || diffDays == 0) {
            return "#ef4444";
        }
        return "#C8E6C9";
    }
}
