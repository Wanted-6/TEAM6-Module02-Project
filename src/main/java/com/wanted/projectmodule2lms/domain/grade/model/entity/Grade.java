package com.wanted.projectmodule2lms.domain.grade.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Integer gradeId;

    @Column(name = "enrollment_id", nullable = false, unique = true)
    private Integer enrollmentId;

    @Column(name = "attendance_score", precision = 5, scale = 2)
    private BigDecimal attendanceScore;

    @Column(name = "assignment_score", precision = 5, scale = 2)
    private BigDecimal assignmentScore;

    @Column(name = "exam_score", precision = 5, scale = 2)
    private BigDecimal examScore;

    @Column(name = "attitude_score", precision = 5, scale = 2)
    private BigDecimal attitudeScore;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "is_passed")
    private Boolean isPassed;

    public Grade(Integer enrollmentId) {
        this.enrollmentId = enrollmentId;
        this.attendanceScore = null;
        this.assignmentScore = null;
        this.examScore = null;
        this.attitudeScore = null;
        this.totalScore = null;
        this.isPassed = null;
    }

    public void updateScore(
            BigDecimal attendanceScore,
            BigDecimal assignmentScore,
            BigDecimal examScore,
            BigDecimal attitudeScore,
            BigDecimal totalScore,
            Boolean isPassed
    ) {
        this.attendanceScore = attendanceScore;
        this.assignmentScore = assignmentScore;
        this.examScore = examScore;
        this.attitudeScore = attitudeScore;
        this.totalScore = totalScore;
        this.isPassed = isPassed;
    }
}