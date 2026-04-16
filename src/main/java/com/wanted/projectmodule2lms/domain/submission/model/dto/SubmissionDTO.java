package com.wanted.projectmodule2lms.domain.submission.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SubmissionDTO {

    private Integer submissionId;
    private Integer assignmentId;
    private Integer enrollmentId;
    private String content;
    private String attachmentFile;
    private LocalDateTime submittedAt;
    private Double score;
    private String feedback;
}