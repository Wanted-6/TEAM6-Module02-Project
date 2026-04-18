package com.wanted.projectmodule2lms.domain.grade.model.dto;

import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GradeUpdateDTO {

    private Integer enrollmentId;
    private BigDecimal assignmentScore;
    private BigDecimal examScore;
    private BigDecimal attitudeScore;
}
