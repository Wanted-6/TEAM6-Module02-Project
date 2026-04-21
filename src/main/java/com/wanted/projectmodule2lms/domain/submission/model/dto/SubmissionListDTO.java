package com.wanted.projectmodule2lms.domain.submission.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SubmissionListDTO {
    private Integer enrollmentId;
    private String studentLoginId;
    private String studentName;
    private String submitStatus;
    private LocalDateTime submittedAt;
    private Integer submissionId;
}
