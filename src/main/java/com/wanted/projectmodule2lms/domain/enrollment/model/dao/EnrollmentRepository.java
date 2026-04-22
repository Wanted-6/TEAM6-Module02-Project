package com.wanted.projectmodule2lms.domain.enrollment.model.dao;

import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.EnrollmentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByMemberId(Integer memberId);

    @Query("select e.courseId from Enrollment e where e.memberId = :memberId")
    List<Integer> findCourseIdsByMemberId(Integer memberId);



}

