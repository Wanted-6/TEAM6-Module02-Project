package com.wanted.projectmodule2lms.global.aop.dto;

public interface LogStatDto {
    String getMethodName();
    Long getCallCount();
    Double getAvgExecutionTime();
}
