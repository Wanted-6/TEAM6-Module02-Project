package com.wanted.projectmodule2lms.domain.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseCreateDTO {

    private Integer courseId;
    private String instructorLoginId;
    private String title;
    private String description;
    private String category;
    private String thumbnailImage;
    private Integer capacity;
    private LocalDate startDate;
    private LocalDate endDate;
}