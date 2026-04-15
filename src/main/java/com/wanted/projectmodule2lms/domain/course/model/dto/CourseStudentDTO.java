package com.wanted.projectmodule2lms.domain.course.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseStudentDTO {

    private Integer enrollmentId;
    private Integer memberId;
    private String loginId;
    private String name;
    private String phone;
    private String email;
    private String enrollmentStatus;
    private LocalDateTime enrolledAt;
}