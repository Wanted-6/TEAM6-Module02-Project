package com.wanted.projectmodule2lms.domain.submission.model.dao;

import com.wanted.projectmodule2lms.domain.submission.model.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

    List<Submission> findByAssignmentIdOrderBySubmittedAtDesc(Integer assignmentId);

    Optional<Submission> findByAssignmentIdAndMemberId(Integer assignmentId, Integer memberId);
}