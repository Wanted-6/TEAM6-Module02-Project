package com.wanted.projectmodule2lms.global.aop;

import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import com.wanted.projectmodule2lms.global.aop.dao.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(com.wanted.projectmodule2lms.global.annotation.AuditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Long currentMemberId = com.wanted.projectmodule2lms.global.util.SecurityUtil.getCurrentMemberId();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            saveLogToDb(currentMemberId, methodName, executionTime, "SUCCESS", null);
            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            saveLogToDb(currentMemberId, methodName, executionTime, "FAIL", e.getMessage());
            throw e;
        }
    }

    // DB 저장
    private void saveLogToDb(Long memberId, String methodName, long executionTime, String status, String errorMessage) {
        com.wanted.projectmodule2lms.global.aop.entity.AuditLog logHistory = com.wanted.projectmodule2lms.global.aop.entity.AuditLog.builder()
                .memberId(memberId)
                .methodName(methodName)
                .executionTime(executionTime)
                .status(status)
                .errorMessage(errorMessage)
                .build();
        auditLogRepository.save(logHistory);
    }

}
