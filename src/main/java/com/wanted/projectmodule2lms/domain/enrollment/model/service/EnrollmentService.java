package com.wanted.projectmodule2lms.domain.enrollment.model.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.exception.DuplicateEnrollmentException;
import com.wanted.projectmodule2lms.domain.enrollment.exception.EnrollmentCapacityExceededException;
import com.wanted.projectmodule2lms.domain.enrollment.exception.EnrollmentLimitExceededException;
import com.wanted.projectmodule2lms.domain.enrollment.exception.EnrollmentNotAllowedException;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.EnrollmentStatus;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public void enrollCourse(Integer memberId, Integer courseId) {
        Course course = courseRepository.findByIdForUpdate(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 강의입니다."));

        if (!Boolean.TRUE.equals(course.getIsOpen())) {
            throw new EnrollmentNotAllowedException("신청할 수 없는 강의입니다.");
        }

        if (enrollmentRepository.existsByMemberIdAndCourseId(memberId, courseId)) {
            throw new DuplicateEnrollmentException("이미 수강신청한 강의입니다.");
        }

        long enrolledCourseCount = enrollmentRepository.countByMemberIdAndStatus(memberId, EnrollmentStatus.ENROLLED);
        if (enrolledCourseCount >= 3) {
            throw new EnrollmentLimitExceededException("최대 3개 강의까지만 수강신청할 수 있습니다.");
        }

        long currentCourseEnrollmentCount = enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED);
        if (currentCourseEnrollmentCount >= course.getCapacity()) {
            throw new EnrollmentCapacityExceededException("정원이 초과되어 수강신청할 수 없습니다.");
        }

        try {
            Enrollment savedEnrollment = enrollmentRepository.save(new Enrollment(memberId, courseId));
            gradeRepository.save(new Grade(savedEnrollment.getEnrollmentId()));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEnrollmentException("이미 수강신청한 강의입니다.");
        }
    }


    @Transactional(readOnly = true)
    public List<Enrollment> findEnrollmentsByMemberId(Integer memberId) {
        return enrollmentRepository.findByMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public boolean isAlreadyEnrolled(Integer memberId, Integer courseId) {
        return enrollmentRepository.existsByMemberIdAndCourseId(memberId, courseId);
    }

    @Transactional(readOnly = true)
    public List<Enrollment> getMyEnrollments(Integer memberId) {
        return enrollmentRepository.findByMemberId(memberId);
    }
}
