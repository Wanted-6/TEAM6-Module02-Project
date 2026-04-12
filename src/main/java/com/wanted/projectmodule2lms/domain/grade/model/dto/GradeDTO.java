package com.wanted.projectmodule2lms.domain.grade.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GradeDTO {

    private Integer gradeId;
    private Integer enrollmentId;
    private String courseTitle;

    private BigDecimal attendanceScore;
    private BigDecimal assignmentScore;
    private BigDecimal examScore;
    private BigDecimal attitudeScore;
    private BigDecimal totalScore;

    private String completionStatus;
}
