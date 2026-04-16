package com.wanted.projectmodule2lms.domain.attendance.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Attendance")
@Getter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Integer attendanceId;

    @Column(name = "enrollment_id", nullable = false)
    private Integer enrollmentId;

    @Column(name = "section_id", nullable = false)
    private Integer sectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt;

    @Column(name = "recorded_at", insertable = false, updatable = false)
    private LocalDateTime recordedAt;

    @Column(name = "is_manual", nullable = false)
    private Boolean isManual;

    @Column(name = "note")
    private String note;

    public Attendance(Integer enrollmentId,
                      Integer sectionId,
                      AttendanceStatus status,
                      LocalDateTime checkedAt,
                      String note) {
        this.enrollmentId = enrollmentId;
        this.sectionId = sectionId;
        this.status = status;
        this.checkedAt = checkedAt;
        this.isManual = false;
        this.note = note;
    }

    public void changeAttendance(AttendanceStatus status, LocalDateTime checkedAt, String note) {
        this.status = status;
        this.checkedAt = checkedAt;
        this.note = note;
    }
}