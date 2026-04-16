package com.wanted.projectmodule2lms.domain.member.model.dao;

import com.wanted.projectmodule2lms.domain.member.model.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByLoginId(String loginId);

    List<LoginLog> findAllByOrderByLoginTimeDesc();
}
