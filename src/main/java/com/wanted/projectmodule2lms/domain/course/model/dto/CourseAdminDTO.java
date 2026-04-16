package com.wanted.projectmodule2lms.domain.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseAdminDTO {

    private String title;
    private String description;
    private String category;
    private String thumbnailImage;
    private Integer capacity;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private Boolean isOpen;
    private String approvalStatus;
    private String rejectReason;

    private String instructorLoginId;
    private String instructorName;

    private String reviewerLoginId;
    private String reviewerName;
}