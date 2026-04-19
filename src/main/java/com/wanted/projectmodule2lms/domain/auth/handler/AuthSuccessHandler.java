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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        LoginMemberDTO loginMember = authDetails.getLoginMemberDTO();
        String username = authentication.getName();

        memberService.resetLoginFailCount(username);

         loginLogService.saveLoginLog(username, true, request.getRemoteAddr());

        memberService.resetLoginFailCount(username);

        if ("INSTRUCTOR".equals(loginMember.getRole())) {

            if ("PENDING".equals(loginMember.getApprovalStatus())) {
                System.out.println("🚨 대기 중이므로 로그인 할 수 없습니다.");
                getRedirectStrategy().sendRedirect(request, response, "/auth/login?error=pending");
                return;
            }

            if ("REJECTED".equals(loginMember.getApprovalStatus())) {
                System.out.println("🚨 신청 반려로 로그인할 수 없습니다.");
                getRedirectStrategy().sendRedirect(request, response, "/auth/login?error=rejected");
                return;
            }

            if ("APPROVED".equals(loginMember.getApprovalStatus()) && !loginMember.isVerified()) {
                System.out.println("🚨 승인코드를 입력하지 않았습니다. 승인 코드를 입력해주세요.");
                getRedirectStrategy().sendRedirect(request, response, "/member/verify-code");
                return;
            }
        }

        if (loginMember.isTempPassword()) {
            System.out.println("🚨 임시 비밀번호 사용자 [" + username + "] 감지: 비밀번호 변경 페이지로 이동합니다.");

            String targetUrl = "/member/edit-password";

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            return;
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

}
