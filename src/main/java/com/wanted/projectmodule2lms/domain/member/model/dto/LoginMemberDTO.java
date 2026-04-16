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

    // 세션에 담아둘 핵심 정보들 (User -> Member 용어로 완벽 통일)
    private Integer memberId;      // 기존 userCode (PK)
    private String loginId;        // 기존 userId (로그인 아이디)
    private String name;           // 기존 userName (실명)
    private String password;       // 비밀번호 (Security 인증 시 필요)
    private String role;           // 기존 userRole (권한 문자열)
    private Integer loginFailCount;    // 로그인 실패 횟수
    private boolean accountLocked; // 기존 isAccountLocked (계정 잠금 여부)

    /**
     * (선택사항) Service 계층에서 아이디, 비밀번호, 권한 3개만 넣어서
     * 객체를 생성할 때 에러가 나지 않도록 만들어둔 맞춤형 생성자
     */
    public LoginMemberDTO(String loginId, String password, String role) {
        this.loginId = loginId;
        this.password = password;
        this.role = role;
    }

    /**
     * 문자열로 된 사용자의 권한을 콤마(,) 기준으로 분리하여 List로 반환하는 메서드
     * * 🚨 주의: Lombok이 기본적으로 'getRole()'이라는 메서드(String 반환)를
     * 자동으로 만들어주기 때문에, 이름이 겹치지 않도록 'getRoleList()'로 변경했습니다!
     */
    public List<String> getRoleList() {
        if (this.role != null && !this.role.trim().isEmpty()) {
            return Arrays.asList(this.role.split(","));
        }
        return new ArrayList<>(); // 다중 권한 = ["STUDENT", "ADMIN"] 등 형태로 리턴
    }
}