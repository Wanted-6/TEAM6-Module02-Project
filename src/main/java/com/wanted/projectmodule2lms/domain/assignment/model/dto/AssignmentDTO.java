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
<<<<<<< HEAD
}
=======
}
>>>>>>> e54066727348eccaac506e4439656ada00f4d5ee
