package com.wanted.projectmodule2lms.domain.enrollment.model.dao;

import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    List<Enrollment> findByMemberId(Integer memberId);

    Optional<Enrollment> findByMemberIdAndCourseId(Integer memberId, Integer courseId);

    List<Enrollment> findByCourseId(Integer courseId);

    List<Enrollment> findByCourseIdOrderByEnrolledAtAsc(Integer courseId);

    List<Enrollment> findByStatus(EnrollmentStatus status);

    boolean existsByMemberIdAndCourseId(Integer memberId, Integer courseId);

    long countByMemberIdAndStatus(Integer memberId, EnrollmentStatus status);

    long countByCourseIdAndStatus(Integer courseId, EnrollmentStatus status);
}

