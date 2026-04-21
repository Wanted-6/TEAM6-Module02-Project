package com.wanted.projectmodule2lms.domain.member.model.entity;

import com.wanted.projectmodule2lms.domain.profile.entity.Profile;
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

    @Column(name = "is_temp_password")
    private boolean isTempPassword = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus;

    @Column(name = "approval_code", length = 10)
    private String approvalCode;

    @Builder.Default
    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "grad_cert_path")
    private String gradCertPath;

    @Column(name = "career_cert_path")
    private String careerCertPath;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Profile profile;

    public void changeToTempPassword(String encodedPassword) {
        this.password = encodedPassword;
        this.isTempPassword = true;
    }

    public void changeRegularPassword(String encodedPassword) {
        this.password = encodedPassword;
        this.isTempPassword = false;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = MemberStatus.ACTIVE;
            if (this.role == MemberRole.INSTRUCTOR && this.approvalStatus == null) {
                this.approvalStatus = ApprovalStatus.PENDING; // 강사 무조건 대기
                this.isVerified = false;
            } else if (this.role != MemberRole.INSTRUCTOR && this.approvalStatus == null) {
                this.approvalStatus = ApprovalStatus.APPROVED; // 학생 바로 승인
                this.isVerified = true;
            }
        }
    }

    public void increaseLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
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

    public void changePassword(String newPassword){
        this.password = newPassword;
    }

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

    public void changeStatus(MemberStatus status) {
        this.status = status;
    }

    public void approveInstructor(String code){
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvalCode = code;
        this.rejectReason = null;
    }

    public void rejectInstructor(String reason){
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.rejectReason = reason;
        this.approvalCode = null;
    }

    public void verifyApprovalCode() {
        this.isVerified = true;
        this.approvalCode = null;
    }

    public void assignProfile(Profile profile) {
        this.profile = profile;
    }

    public void updatePhone(String phone) {
        this.phone = phone;
    }

}
