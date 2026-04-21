package com.wanted.projectmodule2lms.domain.member.model.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class LoginMemberDTO {

    private Integer memberId;
    private String loginId;
    private String name;
    private String password;
    private String role;
    private Integer loginFailCount;
    private boolean accountLocked;
    private boolean tempPassword;
    private String approvalStatus;
    private boolean isVerified;
    private String rejectReason;


    public LoginMemberDTO(String loginId, String password, String role) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
    }

    public List<String> getRoleList() {
        if (this.role != null && !this.role.trim().isEmpty()) {
            return Arrays.asList(this.role.split(","));
        }
        return new ArrayList<>();
    }
}
