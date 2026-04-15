package com.wanted.projectmodule2lms.domain.enrollment.model.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.EnrollmentStatus;
import com.wanted.projectmodule2lms.domain.grade.model.dao.GradeRepository;
import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

        // 1. 공개 강의 여부 확인
        if (!Boolean.TRUE.equals(course.getIsOpen())) {
            throw new IllegalArgumentException("신청할 수 없는 강의입니다.");
        }

        // 2. 중복 신청 확인
        if (enrollmentRepository.existsByMemberIdAndCourseId(memberId, courseId)) {
            throw new IllegalArgumentException("이미 수강신청한 강의입니다.");
        }

        // 3. 학생의 현재 수강 중 강의 수 확인 (최대 3개)



        long enrolledCourseCount =
                enrollmentRepository.countByMemberIdAndStatus(memberId, EnrollmentStatus.ENROLLED);

        if (enrolledCourseCount >= 3) {
            throw new IllegalArgumentException("최대 3개 강의까지만 수강신청할 수 있습니다.");
        }

        // 4. 강의 정원 확인
        long currentCourseEnrollmentCount =
                enrollmentRepository.countByCourseIdAndStatus(courseId, EnrollmentStatus.ENROLLED);

        if (currentCourseEnrollmentCount >= course.getCapacity()) {
            throw new IllegalArgumentException("정원이 초과되어 수강신청할 수 없습니다.");
        }

        // 5. 수강신청 저장
        Enrollment enrollment = new Enrollment(memberId, courseId);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 6. 성적 테이블 초기 생성
        Grade grade = new Grade(savedEnrollment.getEnrollmentId());
        gradeRepository.save(grade);
    }

    public List<Enrollment> findEnrollmentsByMemberId(Integer memberId) {
        return enrollmentRepository.findByMemberId(memberId);
    }

    public boolean isAlreadyEnrolled(Integer memberId, Integer courseId) {
        return enrollmentRepository.existsByMemberIdAndCourseId(memberId, courseId);
    }
}