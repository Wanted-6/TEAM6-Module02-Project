package com.wanted.projectmodule2lms.domain.member.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Integer memberId;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount = 0;

    @Builder.Default
    @Column(name = "is_account_locked", nullable = false)
    private Boolean accountLocked = false;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = MemberStatus.ACTIVE;
        }
    }

    public void increaseLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0; // null이면 0으로 초기화
        }
        if (this.accountLocked == null) {
            this.accountLocked = false;
        }

        this.loginFailCount++;
    }

    public void resetLoginFailCount() {
        this.loginFailCount = 0;
    }

    public void lockAccount() {
        this.accountLocked = true;
    }

/*    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }*/

    public void changePassword(String newPassword){
        this.password = newPassword;
    }

    /**
     * 회원 생성 메서드
     */
    public static Member createMember(String loginId,
                                      String password,
                                      String name,
                                      String phone,
                                      String email,
                                      MemberRole role) {
        return Member.builder()
                .loginId(loginId)
                .password(password)
                .name(name)
                .phone(phone)
                .email(email)
                .role(role)
                .status(MemberStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();   // loginFailCount, accountLock 들어가록 함.
    }

    /**
     * 회원 상태 변경
     */
    public void changeStatus(MemberStatus status) {
        this.status = status;
    }
}