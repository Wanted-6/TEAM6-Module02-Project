package com.wanted.projectmodule2lms.domain.auth.handler;

import com.wanted.projectmodule2lms.domain.auth.model.service.LoginLogService;
import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

public class AuthFailHandler extends SimpleUrlAuthenticationFailureHandler {

    private final MemberService memberService;
    private final LoginLogService loginLogService;

    public AuthFailHandler(MemberService memberService, LoginLogService loginLogService) {
        this.memberService = memberService;
        this.loginLogService = loginLogService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        String errorMessage;
        String username = request.getParameter("memberId");

        if (exception instanceof BadCredentialsException) {
            errorMessage = "존재하지 않는 아이디거나 비밀번호가 일치하지 않습니다.";

            if (username != null && !username.isEmpty()) {
                int failCount = memberService.incrementLoginFailCount(username);

                if (failCount >0 ) {
                    errorMessage += " (실패 횟수: " + failCount + "회)";
                }
            }
        } else if (exception instanceof LockedException) {
            errorMessage = "계정이 잠겨있습니다. 관리자에게 문의하세요.";
        } else if (exception instanceof InternalAuthenticationServiceException) {
            errorMessage = "서버에서 오류가 발생되었습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 아이디입니다.";
        } else if (exception instanceof AuthenticationCredentialsNotFoundException) {
            errorMessage = "인증 요청이 거부되었습니다.";
        } else {
            errorMessage = "알 수 없는 오류로 로그인 요청을 처리할 수 없습니다.";
        }

        loginLogService.saveLoginLog(username,false, request.getRemoteAddr());

        request.getSession().setAttribute("errorMessage", errorMessage);
        response.sendRedirect("/auth/fail");
    }
}
