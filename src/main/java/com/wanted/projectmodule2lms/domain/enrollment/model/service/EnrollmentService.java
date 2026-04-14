package com.wanted.projectmodule2lms.domain.enrollment.model.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public void enrollCourse(Integer memberId, Integer courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (!Boolean.TRUE.equals(course.getIsOpen())) {
            throw new IllegalArgumentException("신청할 수 없는 강의입니다.");
        }

        enrollmentRepository.findByMemberIdAndCourseId(memberId, courseId)
                .ifPresent(enrollment -> {
                    throw new IllegalArgumentException("이미 수강신청한 강의입니다.");
                });

        Enrollment enrollment = new Enrollment(memberId, courseId);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        Grade grade = new Grade(savedEnrollment.getEnrollmentId());
        gradeRepository.save(grade);
    }
}