package com.wanted.projectmodule2lms.domain.calendar.model.service;

import com.wanted.projectmodule2lms.domain.calendar.model.dao.CalendarMemoRepository;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoCreateDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarMemoDTO;
import com.wanted.projectmodule2lms.domain.calendar.model.entity.CalendarMemo;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalenderService {
    private final CalendarMemoRepository calendarMemoRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;

    public List<CalendarEventDTO> findStudentCalendarEvents(Integer memberId) {

        List<CalendarEventDTO> result = new java.util.ArrayList<>();

        List<Integer> courseIds = enrollmentRepository.findByMemberId(memberId)
                .stream()
                .map(Enrollment::getCourseId)
                .toList();

        if (!courseIds.isEmpty()) {
            List<Course> courses = courseRepository.findByCourseIdIn(courseIds);

            Map<Integer, String> courseTitleMap = courses.stream()
                    .collect(Collectors.toMap(
                            Course::getCourseId,
                            Course::getTitle
                    ));

            List<Section> sections = sectionRepository.findByCourseIdIn(courseIds);

            List<CalendarEventDTO> sectionEvents = sections.stream()
                    .filter(section -> section.getOpenDate() != null)
                    .map(section -> {
                        String courseTitle = courseTitleMap.getOrDefault(section.getCourseId(), "강의");
                        return new CalendarEventDTO(
                                "section-" + section.getSectionId(),
                                courseTitle + " - " + section.getTitle(),
                                section.getOpenDate().toString(),
                                "#3b82f6"
                        );
                    })
                    .collect(Collectors.toList());

            result.addAll(sectionEvents);

            List<CalendarEventDTO> examEvents = courses.stream()
                    .filter(course -> course.getExamDueDate() != null)
                    .map(course -> new CalendarEventDTO(
                            "exam-" + course.getCourseId(),
                            course.getTitle() + " - 시험 마감",
                            course.getExamDueDate().toString(),
                            "#ef4444"
                    ))
                    .collect(Collectors.toList());

            result.addAll(examEvents);
        }

        List<CalendarEventDTO> memoEvents = calendarMemoRepository.findByMemberId(memberId)
                .stream()
                .map(memo -> new CalendarEventDTO(
                        "memo-" + memo.getMemoId(),
                        "[메모] " + memo.getContent(),
                        memo.getMemoDate().toString(),
                        "#C8E6C9"
                ))
                .collect(Collectors.toList());

        result.addAll(memoEvents);



        return result;


    }

    public List<CalendarEventDTO> findInstructorCalendarEvents(Integer instructorId) {

        List<CalendarEventDTO> result = new java.util.ArrayList<>();

        List<Course> courses = courseRepository.findByInstructorId(instructorId);

        if (!courses.isEmpty()) {
            List<Integer> courseIds = courses.stream()
                    .map(Course::getCourseId)
                    .toList();

            Map<Integer, String> courseTitleMap = courses.stream()
                    .collect(Collectors.toMap(
                            Course::getCourseId,
                            Course::getTitle
                    ));

            List<Section> sections = sectionRepository.findByCourseIdIn(courseIds);

            List<CalendarEventDTO> sectionEvents = sections.stream()
                    .filter(section -> section.getOpenDate() != null)
                    .map(section -> {
                        String courseTitle = courseTitleMap.getOrDefault(section.getCourseId(), "강의");
                        return new CalendarEventDTO(
                                "section-" + section.getSectionId(),
                                courseTitle + " - " + section.getTitle(),
                                section.getOpenDate().toString(),
                                "#16a34a"
                        );
                    })
                    .collect(Collectors.toList());

            result.addAll(sectionEvents);
        }

        List<CalendarEventDTO> memoEvents = calendarMemoRepository.findByMemberId(instructorId)
                .stream()
                .map(memo -> new CalendarEventDTO(
                        "memo-" + memo.getMemoId(),
                        "[메모] " + memo.getContent(),
                        memo.getMemoDate().toString(),
                        "#C8E6C9"
                ))
                .collect(Collectors.toList());

        result.addAll(memoEvents);

        List<CalendarEventDTO> examEvents = courses.stream()
                .filter(course -> course.getExamDueDate() != null)
                .map(course -> new CalendarEventDTO(
                        "exam-" + course.getCourseId(),
                        course.getTitle() + " - 시험 마감",
                        course.getExamDueDate().toString(),
                        "#ef4444"
                ))
                .collect(Collectors.toList());

        result.addAll(examEvents);

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
}