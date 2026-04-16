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

        // 1. 메서드 이름 가져오기 (inputData 추출 코드 삭제됨)
        String methodName = joinPoint.getSignature().toShortString();

        // 2. 로그인한 회원의 member_id 가져오기 (pk 대체 값으로 가져옴.)
        Long currentMemberId = com.wanted.projectmodule2lms.global.util.SecurityUtil.getCurrentMemberId();

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 성공 시 DB 저장 (inputData 파라미터 빠짐)
            saveLogToDb(currentMemberId, methodName, executionTime, "SUCCESS", null);
            return result;

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // 실패(에러) 시 에러 메시지와 함께 DB 저장
            saveLogToDb(currentMemberId, methodName, executionTime, "FAIL", e.getMessage());
            throw e;
        }
    }

    // DB 저장 (여기서도 inputData 파라미터 삭제)
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

//    // 시큐리티 세션에서 ID 꺼내오기
//    private Long getCurrentMemberId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
//            return null;
//        }
//
//        Object principal = authentication.getPrincipal();
//
//        if (principal instanceof LoginMemberDTO) {
//            Integer memberId = ((LoginMemberDTO) principal).getMemberId();
//            return memberId != null ? memberId.longValue() : null;
//        }
//
//        return null;
//    }
}