package com.wanted.projectmodule2lms.domain.submission.model.dto;

import com.wanted.projectmodule2lms.domain.submission.model.entity.SubmissionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SubmissionDTO {

    private Integer submissionId;
    private Integer assignmentId;
    private Integer memberId;
    private String content;
    private String submissionFile;
    private LocalDateTime submittedAt;
    private SubmissionStatus status;
    private Integer score;
    private String feedback;
}