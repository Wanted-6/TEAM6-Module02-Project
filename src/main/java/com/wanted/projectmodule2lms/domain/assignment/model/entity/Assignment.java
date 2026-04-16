package com.wanted.projectmodule2lms.domain.assignment.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Assignment")
@Getter
@NoArgsConstructor
public class Assignment {

    @Id
    @Column(name = "assignment_id")
    private Integer assignmentId;

    @Column(name = "section_id", nullable = false)
    private Integer sectionId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "attachment_file")
    private String attachmentFile;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;
}
