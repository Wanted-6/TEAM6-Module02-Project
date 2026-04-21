package com.wanted.projectmodule2lms.global.aop.dao;

import com.wanted.projectmodule2lms.global.aop.dto.LogStatDto;
import com.wanted.projectmodule2lms.global.aop.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a.methodName AS methodName, COUNT(a) AS callCount, AVG(a.executionTime) AS avgExecutionTime " +
            "FROM AuditLog a " +
            "GROUP BY a.methodName " +
            "ORDER BY callCount DESC")
    List<LogStatDto> findMethodStatistics();
}
