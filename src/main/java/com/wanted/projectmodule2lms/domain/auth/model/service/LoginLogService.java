package com.wanted.projectmodule2lms.domain.auth.model.service;

import com.wanted.projectmodule2lms.domain.member.model.dao.LoginLogRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.LoginLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Transactional
    public void saveLoginLog(String loginId, boolean success, String ipAddress){
        LoginLog loginLog = new LoginLog(
                loginId,
                LocalDateTime.now(),
                success,
                ipAddress
        );
        loginLogRepository.save(loginLog);
    }
}
