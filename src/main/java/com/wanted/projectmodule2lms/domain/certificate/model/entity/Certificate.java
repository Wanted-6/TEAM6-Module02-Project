package com.wanted.projectmodule2lms.domain.certificate.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Certificate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "certificate_id")
    private Integer certificateId;

    @Column(name = "enrollment_id", nullable = false, unique = true)
    private Integer enrollmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CertificateStatus status;

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(name = "is_override", nullable = false)
    private Boolean isOverride;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "revoked_by")
    private Integer revokedBy;

    @Column(name = "certificate_file_path")
    private String certificateFilePath;

    public Certificate(Integer enrollmentId, BigDecimal totalScore) {
        this.enrollmentId = enrollmentId;
        this.status = CertificateStatus.REQUESTED;
        this.totalScore = totalScore;
        this.isOverride = false;
        this.requestedAt = LocalDateTime.now();
    }

    public void approve(Integer approvedBy) {
        this.status = CertificateStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
    }

    public void issue(String certificateFilePath) {
        this.status = CertificateStatus.ISSUED;
        this.certificateFilePath = certificateFilePath;
        this.issuedAt = LocalDateTime.now();
    }

    public void revoke(Integer revokedBy) {
        this.status = CertificateStatus.REVOKED;
        this.revokedBy = revokedBy;
        this.revokedAt = LocalDateTime.now();
    }

    public void changeOverride(Boolean isOverride) {
        this.isOverride = isOverride;
    }
}