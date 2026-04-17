package com.wanted.projectmodule2lms.domain.submission.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Submission")
@Getter
@NoArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer submissionId;

    @Column(name = "assignment_id", nullable = false)
    private Integer assignmentId;

    @Column(name = "enrollment_id", nullable = false)
    private Integer enrollmentId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "attachment_file", length = 255)
    private String attachmentFile;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "score")
    private Double score;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    public Submission(Integer assignmentId,
                      Integer enrollmentId,
                      String content,
                      String attachmentFile,
                      LocalDateTime submittedAt) {
        this.assignmentId = assignmentId;
        this.enrollmentId = enrollmentId;
        this.content = content;
        this.attachmentFile = attachmentFile;
        this.submittedAt = submittedAt;
    }

    public void changeSubmission(String content, String attachmentFile) {
        this.content = content;
        this.attachmentFile = attachmentFile;
        this.submittedAt = LocalDateTime.now();
    }

    public void changeScoreAndFeedback(Double score, String feedback) {
        this.score = score;
        this.feedback = feedback;
    }
}