package com.wanted.projectmodule2lms.domain.grade.model.dao;

import com.wanted.projectmodule2lms.domain.grade.model.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface GradeRepository extends JpaRepository<Grade, Integer> {

    // 특정 수강(enrollment)에 대한 성적 조회
    Optional<Grade> findByEnrollmentId(Integer enrollmentId);

    // 여러 수강(enrollment)에 대한 성적 조회
    List<Grade> findByEnrollmentIdIn(List<Integer> enrollmentIds);

}
