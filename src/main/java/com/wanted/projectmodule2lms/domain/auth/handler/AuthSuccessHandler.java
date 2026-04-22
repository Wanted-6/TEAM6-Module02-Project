package com.wanted.projectmodule2lms.domain.auth.handler;

import com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails;
import com.wanted.projectmodule2lms.domain.auth.model.service.LoginLogService;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;
    private final LoginLogService loginLogService;

    public AuthSuccessHandler(MemberService memberService, LoginLogService loginLogService) {
        this.memberService = memberService;
        this.loginLogService = loginLogService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        LoginMemberDTO loginMember = authDetails.getLoginMemberDTO();
        String username = authentication.getName();

        memberService.resetLoginFailCount(username);
        loginLogService.saveLoginLog(username, true, request.getRemoteAddr());

        if ("INSTRUCTOR".equals(loginMember.getRole())) {
            if ("PENDING".equals(loginMember.getApprovalStatus())) {
                getRedirectStrategy().sendRedirect(request, response, "/auth/login?error=pending");
                return;
            }

            if ("REJECTED".equals(loginMember.getApprovalStatus())) {
                getRedirectStrategy().sendRedirect(request, response, "/auth/login?error=rejected");
                return;
            }

            if ("APPROVED".equals(loginMember.getApprovalStatus()) && !loginMember.isVerified()) {
                getRedirectStrategy().sendRedirect(request, response, "/member/verify-code");
                return;
            }
        }

        if (loginMember.isTempPassword()) {
            getRedirectStrategy().sendRedirect(request, response, "/member/edit-password");
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
