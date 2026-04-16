package com.wanted.projectmodule2lms.domain.assignment.model.dao;

import com.wanted.projectmodule2lms.domain.assignment.model.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> e54066727348eccaac506e4439656ada00f4d5ee
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {

<<<<<<< HEAD
    List<Assignment> findBySectionIdOrderByDueDateAsc(Integer sectionId);

    Optional<Assignment> findById(Integer assignmentId);
=======
    Optional<Assignment> findFirstBySectionIdOrderByAssignmentIdAsc(Integer sectionId);
>>>>>>> e54066727348eccaac506e4439656ada00f4d5ee
}
