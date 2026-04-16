package com.wanted.projectmodule2lms.domain.assignment.model.entity;

<<<<<<< HEAD
import jakarta.persistence.*;
=======
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
>>>>>>> e54066727348eccaac506e4439656ada00f4d5ee
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Assignment")
@Getter
@NoArgsConstructor
public class Assignment {

    @Id
<<<<<<< HEAD
    @GeneratedValue(strategy = GenerationType.IDENTITY)
=======
>>>>>>> e54066727348eccaac506e4439656ada00f4d5ee
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
<<<<<<< HEAD

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
=======
}
>>>>>>> e54066727348eccaac506e4439656ada00f4d5ee
