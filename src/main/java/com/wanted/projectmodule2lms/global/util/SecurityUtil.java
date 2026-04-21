package com.wanted.projectmodule2lms.global.util;

import com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentMemberId() {
        // 시큐리티 컨텍스트에서 현재 인증 정보(Authentication) 가져옴.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나, 비로그인 상태면 null 반환함.
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        // Principal 객체를 만들어진 DTO로 형변환해서 PK만 빼서 반환.
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails authDetails) {
            com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO loginMember = authDetails.getLoginMemberDTO();
            if (loginMember != null && loginMember.getMemberId() != null) {
                return loginMember.getMemberId().longValue();
            }
        }

        return null;
    }
}




