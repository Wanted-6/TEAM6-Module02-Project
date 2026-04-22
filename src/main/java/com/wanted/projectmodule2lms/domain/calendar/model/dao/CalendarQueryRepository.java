package com.wanted.projectmodule2lms.domain.calendar.model.dao;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.entity.CalendarMemo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CalendarQueryRepository extends JpaRepository<CalendarMemo, Integer> {

    @Query("""
        select new com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO(
            concat('section-', s.sectionId),
            concat(c.title, ' - ', s.title),
            concat('', s.openDate),
            '#3b82f6',
            c.courseId
        )
        from Enrollment e
        join Course c on c.courseId = e.courseId
        join Section s on s.courseId = c.courseId
        where e.memberId = :memberId
          and s.openDate is not null
        order by s.openDate asc, s.sectionId asc
    """)
    List<CalendarEventDTO> findStudentSectionEvents(@Param("memberId") Integer memberId);

    @Query("""
        select new com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO(
            concat('assignment-', a.assignmentId),
            concat(c.title, ' - 과제 마감'),
            substring(concat('', a.dueDate), 1, 10),
            '#a78bfa',
            c.courseId
        )
        from Enrollment e
        join Course c on c.courseId = e.courseId
        join Assignment a on a.courseId = c.courseId
        where e.memberId = :memberId
          and a.dueDate is not null
        order by a.dueDate asc, a.assignmentId asc
    """)
    List<CalendarEventDTO> findStudentAssignmentEvents(@Param("memberId") Integer memberId);

    @Query("""
        select new com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO(
            concat('exam-', c.courseId),
            concat(c.title, ' - 시험 마감'),
            concat('', c.examDueDate),
            '#ef4444',
            c.courseId
        )
        from Enrollment e
        join Course c on c.courseId = e.courseId
        where e.memberId = :memberId
          and c.examDueDate is not null
        order by c.examDueDate asc, c.courseId asc
    """)
    List<CalendarEventDTO> findStudentExamEvents(@Param("memberId") Integer memberId);

    @Query("""
        select new com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO(
            concat('section-', s.sectionId),
            concat(c.title, ' - ', s.title),
            concat('', s.openDate),
            '#16a34a',
            c.courseId
        )
        from Course c
        join Section s on s.courseId = c.courseId
        where c.instructorId = :instructorId
          and s.openDate is not null
        order by s.openDate asc, s.sectionId asc
    """)
    List<CalendarEventDTO> findInstructorSectionEvents(@Param("instructorId") Integer instructorId);

    @Query("""
        select new com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO(
            concat('assignment-', a.assignmentId),
            concat(c.title, ' - 과제 마감'),
            substring(concat('', a.dueDate), 1, 10),
            '#a78bfa',
            c.courseId
        )
        from Course c
        join Assignment a on a.courseId = c.courseId
        where c.instructorId = :instructorId
          and a.dueDate is not null
        order by a.dueDate asc, a.assignmentId asc
    """)
    List<CalendarEventDTO> findInstructorAssignmentEvents(@Param("instructorId") Integer instructorId);

    @Query("""
        select new com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO(
            concat('exam-', c.courseId),
            concat(c.title, ' - 시험 마감'),
            concat('', c.examDueDate),
            '#ef4444',
            c.courseId
        )
        from Course c
        where c.instructorId = :instructorId
          and c.examDueDate is not null
        order by c.examDueDate asc, c.courseId asc
    """)
    List<CalendarEventDTO> findInstructorExamEvents(@Param("instructorId") Integer instructorId);
}
