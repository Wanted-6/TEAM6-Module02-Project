package com.wanted.projectmodule2lms.domain.course.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Course")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "instructor_id", nullable = false)
    private Integer instructorId;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "thumbnail_image", length = 255)
    private String thumbnailImage;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false)
    private CourseApprovalStatus approvalStatus;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Column(name = "reviewed_by")
    private Integer reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void changeCourseInfo(String title,
                                 String description,
                                 String category,
                                 String thumbnailImage,
                                 Integer capacity,
                                 LocalDate startDate,
                                 LocalDate endDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.thumbnailImage = thumbnailImage;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void changeOpenStatus(Boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void submitForApproval() {
        this.isOpen = false;
        this.approvalStatus = CourseApprovalStatus.PENDING;
        this.rejectReason = null;
        this.reviewedBy = null;
        this.reviewedAt = null;
        this.deletedAt = null;
    }

    public void approve(Integer adminId) {
        this.isOpen = true;
        this.approvalStatus = CourseApprovalStatus.APPROVED;
        this.rejectReason = null;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    public void reject(Integer adminId, String reason) {
        this.isOpen = false;
        this.approvalStatus = CourseApprovalStatus.REJECTED;
        this.rejectReason = reason;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.deletedAt = null;
    }

    public void markDeleted(Integer adminId) {
        this.isOpen = false;
        this.approvalStatus = CourseApprovalStatus.DELETED;
        this.reviewedBy = adminId;
        this.reviewedAt = LocalDateTime.now();
        this.deletedAt = LocalDateTime.now();
    }
}