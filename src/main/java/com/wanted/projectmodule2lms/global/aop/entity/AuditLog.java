package com.wanted.projectmodule2lms.global.aop.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "auditlog")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private String methodName;

    private long executionTime;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime createdAt;

    @Builder
    public AuditLog(Long memberId, String methodName, long executionTime, String status, String errorMessage) {
        this.memberId = memberId;
        this.methodName = methodName;
        this.executionTime = executionTime;
        this.status = status;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }
}
