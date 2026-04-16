package com.wanted.projectmodule2lms.domain.assignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AssignmentDTO {

    private Integer assignmentId;
    private Integer sectionId;
    private String title;
    private String description;
    private String attachmentFile;
    private LocalDateTime dueDate;
}
