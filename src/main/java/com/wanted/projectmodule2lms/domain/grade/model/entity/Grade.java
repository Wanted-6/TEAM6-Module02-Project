package com.wanted.projectmodule2lms.domain.grade.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Grade")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "grade_id")
    private Integer gradeId;

    @Column(name = "enrollment_id", nullable = false, unique = true)
    private Integer enrollmentId;

    @Column(name = "attendance_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal attendanceScore;

    @Column(name = "assignment_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal assignmentScore;

    @Column(name = "exam_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal examScore;

    @Column(name = "attitude_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal attitudeScore;

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "is_passed", nullable = false)
    private Boolean isPassed;
}