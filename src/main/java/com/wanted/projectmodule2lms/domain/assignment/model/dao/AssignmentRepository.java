package com.wanted.projectmodule2lms.domain.assignment.model.dao;

import com.wanted.projectmodule2lms.domain.assignment.model.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

    List<Assignment> findBySectionIdOrderByDueDateAsc(Integer sectionId);

    Optional<Assignment> findById(Integer assignmentId);
}
