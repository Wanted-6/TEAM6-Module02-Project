package com.wanted.projectmodule2lms.domain.calendar.model.service;

import com.wanted.projectmodule2lms.domain.calendar.model.dto.CalendarEventDTO;
import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalenderService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;

    public List<CalendarEventDTO> findStudentCalendarEvents(Integer memberId) {

        List<Integer> courseIds = enrollmentRepository.findByMemberId(memberId)
                .stream()
                .map(Enrollment::getCourseId)
                .toList();

        if (courseIds.isEmpty()) {
            return List.of();
        }

        List<Course> courses = courseRepository.findByCourseIdIn(courseIds);

        Map<Integer, String> courseTitleMap = courses.stream()
                .collect(Collectors.toMap(
                        Course::getCourseId,
                        Course::getTitle
                ));

        List<Section> sections = sectionRepository.findByCourseIdIn(courseIds);

        return sections.stream()
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
    }
}