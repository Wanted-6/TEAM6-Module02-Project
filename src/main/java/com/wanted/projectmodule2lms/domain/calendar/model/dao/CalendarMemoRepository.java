package com.wanted.projectmodule2lms.domain.calendar.model.dao;

import com.wanted.projectmodule2lms.domain.calendar.model.entity.CalendarMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CalendarMemoRepository extends JpaRepository<CalendarMemo, Integer> {

    List<CalendarMemo> findByMemberId(Integer memberId);

    List<CalendarMemo> findByMemberIdAndMemoDate(Integer memberId, LocalDate memoDate);
}