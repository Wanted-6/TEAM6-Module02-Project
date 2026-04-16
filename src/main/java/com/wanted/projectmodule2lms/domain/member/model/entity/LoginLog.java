package com.wanted.projectmodule2lms.domain.member.model.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_log") // ⭐ 새로 만든 테이블 이름!
@Getter
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 누가 로그인을 시도했는지 (성공하든 실패하든 입력한 아이디 저장)
    @Column(name = "login_id", nullable = false, length = 50)
    private String loginId;

    // 로그인 시도 시간
    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    // 성공 여부 (true: 성공, false: 실패)
    @Column(name = "success", nullable = false)
    private boolean success;

    // 접속 IP 주소
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    protected LoginLog() {} // JPA 기본 생성자

    // 생성자
    public LoginLog(String loginId, LocalDateTime loginTime, boolean success, String ipAddress) {
        this.loginId = loginId;
        this.loginTime = loginTime;
        this.success = success;
        this.ipAddress = ipAddress;
    }
}