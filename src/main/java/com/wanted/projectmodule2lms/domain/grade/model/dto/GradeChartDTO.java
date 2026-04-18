package com.wanted.projectmodule2lms.domain.grade.model.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GradeChartDTO {
    private Double attendance;
    private Double assignment;
    private Double exam;
    private Double attitude;
    private Double total;
}
