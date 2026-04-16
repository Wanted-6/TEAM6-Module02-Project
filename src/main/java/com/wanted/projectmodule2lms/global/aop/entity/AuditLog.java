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

    private Long memberId; // 로그인한 회원 번호

    private String methodName; // 실행된 메서드 이름

    private long executionTime; // 실행 시간

    private String status; // 성공/실패 여부

    @Column(columnDefinition = "TEXT")
    private String errorMessage; // 에러 메시지

    private LocalDateTime createdAt; // 생성 시간

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