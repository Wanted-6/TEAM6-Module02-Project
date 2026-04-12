package com.wanted.projectmodule2lms.domain.enrollment.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EnrollmentCreateDTO {
    private Integer memberId;
    private Integer courseId;
}