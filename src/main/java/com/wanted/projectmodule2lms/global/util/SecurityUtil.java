package com.wanted.projectmodule2lms.global.util;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
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




