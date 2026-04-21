package com.wanted.projectmodule2lms.domain.submission.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionScoreDTO {

    private Double score;
    private String feedback;
}