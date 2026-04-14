package com.wanted.projectmodule2lms.domain.grade.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class GradeUpdateDTO {

    private Integer enrollmentId;
    private BigDecimal assignmentScore;
    private BigDecimal examScore;
    private BigDecimal attitudeScore;
}