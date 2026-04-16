package com.wanted.projectmodule2lms.domain.assignment.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Assignment")
@Getter
@NoArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public Assignment(Integer sectionId,
                      String title,
                      String description,
                      String attachmentFile,
                      LocalDateTime dueDate) {
        this.sectionId = sectionId;
        this.title = title;
        this.description = description;
        this.attachmentFile = attachmentFile;
        this.dueDate = dueDate;
    }

    public void changeAssignmentInfo(String title,
                                     String description,
                                     String attachmentFile,
                                     LocalDateTime dueDate) {
        this.title = title;
        this.description = description;
        this.attachmentFile = attachmentFile;
        this.dueDate = dueDate;
    }
}