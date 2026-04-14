package com.wanted.projectmodule2lms.domain.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseDTO {

    private Integer courseId;
    private Integer instructorId;
    private String title;
    private String description;
    private String category;
    private String thumbnailImage;
    private Integer capacity;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isOpen;

    private String approvalStatus;
    private String rejectReason;
    private Integer reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime deletedAt;
}