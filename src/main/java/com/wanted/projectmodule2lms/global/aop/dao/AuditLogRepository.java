package com.wanted.projectmodule2lms.global.aop.dao;

import com.wanted.projectmodule2lms.global.aop.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {



}
