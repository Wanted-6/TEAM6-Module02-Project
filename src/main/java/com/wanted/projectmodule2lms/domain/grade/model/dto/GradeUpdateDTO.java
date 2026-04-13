package com.wanted.projectmodule2lms.domain.grade.model.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GradeUpdateDTO {

    private Integer enrollmentId;
    private BigDecimal attendanceScore;
    private BigDecimal assignmentScore;
    private BigDecimal examScore;
    private BigDecimal attitudeScore;
}