package com.wanted.projectmodule2lms.domain.assignment.model.dao;

import com.wanted.projectmodule2lms.domain.assignment.model.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    Optional<Assignment> findByCourseId(Integer courseId);

    Optional<Assignment> findFirstByCourseIdOrderByAssignmentIdAsc(Integer courseId);

    List<Assignment> findByCourseIdIn(List<Integer> courseIds);
}
