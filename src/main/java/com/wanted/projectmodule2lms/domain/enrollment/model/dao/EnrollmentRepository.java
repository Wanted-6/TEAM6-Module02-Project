package com.wanted.projectmodule2lms.domain.enrollment.model.dao;

import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

    // 특정 학생의 수강 목록 조회
    List<Enrollment> findByMemberId(Integer memberId);

    // 특정 학생의 특정 강의 수강 여부 확인
    Optional<Enrollment> findByMemberIdAndCourseId(Integer memberId, Integer courseId);

    // 특정 강의 수강생 조회
    List<Enrollment> findByCourseId(Integer courseId);

    // 특정 상태의 수강 목록 조회
    List<Enrollment> findByStatus(EnrollmentStatus status);
}