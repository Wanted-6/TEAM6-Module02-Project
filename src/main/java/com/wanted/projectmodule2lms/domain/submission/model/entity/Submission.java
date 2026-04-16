package com.wanted.projectmodule2lms.domain.submission.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Submission")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Integer submissionId;

    @Column(name = "assignment_id", nullable = false)
    private Integer assignmentId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "submission_file", length = 255)
    private String submissionFile;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubmissionStatus status;

    @Column(name = "score")
    private Integer score;

    @Column(name = "feedback", length = 2000)
    private String feedback;

    public Submission(Integer assignmentId,
                      Integer memberId,
                      String content,
                      String submissionFile,
                      LocalDateTime submittedAt,
                      SubmissionStatus status) {
        this.assignmentId = assignmentId;
        this.memberId = memberId;
        this.content = content;
        this.submissionFile = submissionFile;
        this.submittedAt = submittedAt;
        this.status = status;
    }

    public void changeScoreAndFeedback(Integer score, String feedback) {
        this.score = score;
        this.feedback = feedback;
        this.status = SubmissionStatus.GRADED;
    }
}