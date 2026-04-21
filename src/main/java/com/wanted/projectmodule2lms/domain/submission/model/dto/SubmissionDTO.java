package com.wanted.projectmodule2lms.domain.submission.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
    private String studentName;
    private String studentLoginId;
}
