package com.wanted.projectmodule2lms.domain.member.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_log")
@Getter
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "success", nullable = false)
    private boolean success;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    protected LoginLog() {}

    public LoginLog(String loginId, LocalDateTime loginTime, boolean success, String ipAddress) {
        this.loginId = loginId;
        this.loginTime = loginTime;
        this.success = success;
        this.ipAddress = ipAddress;
    }
}
