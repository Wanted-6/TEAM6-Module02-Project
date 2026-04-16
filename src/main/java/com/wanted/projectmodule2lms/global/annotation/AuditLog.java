package com.wanted.projectmodule2lms.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// (METHOD = 메서드 위에만 붙일 수 있음)
@Target(ElementType.METHOD)

// (RUNTIME = 프로그램이 실행되는 동안 계속 유지)
@Retention(RetentionPolicy.RUNTIME)

// 스티커 이름 정의 - (@interface 키워드 사용)
public @interface AuditLog {
//
//    // (선택) 스티커를 붙일 때 추가 정보를 적을 수 있도록. 예) @AuditLog(actionType = "LOGIN")
//    String actionType() default "SYSTEM";
//
//    String targetType() default "SYSTEM";
}