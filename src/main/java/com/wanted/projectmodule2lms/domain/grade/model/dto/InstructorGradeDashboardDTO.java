package com.wanted.projectmodule2lms.domain.grade.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InstructorGradeDashboardDTO {
    private Integer totalStudentCount;
    private Integer averageProgressRate;
    private Integer lateCount;
    private Integer absentCount;
    private Integer missingAssignmentStudentCount;
}
