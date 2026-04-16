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
public class CourseInstructorDTO {

    private String loginId;
    private String name;
    private String phone;
    private String email;
}