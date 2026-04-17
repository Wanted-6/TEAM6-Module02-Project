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


/**
 * 사용자의 로그인 성공 시
 * 성공 요청을 커스텀 하기 위한 핸들러이다.
 *
 * 로그인 성공 시 실패 횟수를 초기화하여
 * 4번 실패 후 성공하면 카운트를 0으로 리셋한다.
 *
 * 추가 기능
 * 1. 로그인 성공 시 실패 횟수 초기화
 * 2. 로그인 성공 로그 저장
 * */
public class AuthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberService memberService;
    private final LoginLogService loginLogService;
    //    private final LoginLogRepository loginLogRepository;


    public AuthSuccessHandler(MemberService memberService, LoginLogService loginLogService) {
        this.memberService = memberService;
        this.loginLogService = loginLogService;
    }

    /**
     * 사용자의 성공적인 로그인을 처리하기 위한 핸들러이다.
     * 로그인 성공 시 해당 사용자의 실패 횟수를 0으로 초기화한다.
     *
     * @param request 사용자 요청 개체
     * @param response 서버 응답값
     * @param authentication 인증된 사용자 정보
     * */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        LoginMemberDTO loginMember = authDetails.getLoginMemberDTO();
        // 인증된 사용자의 아이디(username) 가져오기
        String username = authentication.getName();

        // 로그인 성공 시 실패 횟수 초기화
        memberService.resetLoginFailCount(username);

        // 로그인 성공 로그 저장
        // 성공 시에는 인증이 완료된 객체(authentication)에서 username을 가져와 기록한다.
         loginLogService.saveLoginLog(username, true, request.getRemoteAddr());

//        // 기본 로그인 성공 처리 (defaultSuccessUrl 또는 savedRequestUrl로 리다이렉트)
//        super.onAuthenticationSuccess(request, response, authentication);

        memberService.resetLoginFailCount(username);

        // 강사 승인 대기 중 & 반려된 경우
        if ("INSTRUCTOR".equals(loginMember.getRole())) {

            if ("PENDING".equals(loginMember.getApprovalStatus())) {
                System.out.println("🚨 대기 중이므로 로그인 할 수 없습니다.");
                // 에러 메시지와 함께 로그인 페이지로 돌려보냄
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