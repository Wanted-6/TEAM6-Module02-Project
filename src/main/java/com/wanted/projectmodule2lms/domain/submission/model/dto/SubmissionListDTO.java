package com.wanted.projectmodule2lms.domain.submission.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SubmissionListDTO {

    private Integer submissionId;
    private String studentName;
    private String studentLoginId;
    private LocalDateTime submittedAt;
    private Double score;
}
