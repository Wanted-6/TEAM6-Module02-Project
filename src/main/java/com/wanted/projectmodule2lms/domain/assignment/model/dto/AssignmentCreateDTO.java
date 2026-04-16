package com.wanted.projectmodule2lms.domain.assignment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreateDTO {

    private String title;
    private String description;
    private String attachmentFile;
    private LocalDateTime dueDate;
}
