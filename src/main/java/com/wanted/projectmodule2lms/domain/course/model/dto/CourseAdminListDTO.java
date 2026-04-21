package com.wanted.projectmodule2lms.domain.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CourseAdminListDTO {

    private Integer courseId;
    private String title;
    private String instructorLoginId;
    private String instructorName;
    private String approvalStatus;
    private Boolean isOpen;
}
