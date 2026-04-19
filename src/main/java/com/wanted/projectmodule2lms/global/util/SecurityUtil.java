package com.wanted.projectmodule2lms.global.util;

import com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

//    View 에서 Session 에 저장 된 회원의 PK 를 hidden input 같은 걸로 넘기면,
//    사용자가 값을 바꿔서 다른 사람 ID로 글을 등록하는 것처럼 조작할 수 있다.
//    반면 서버에서 현재 로그인한 사용자를 기준으로 작성자를 정하면, 클라이언트가 작성자 정보를 마음대로 바꿀 수 없다.
//    @AuthenticationPrincipal AuthDetails authDetails 를 활용해서 로그인 한 사용자의 정보를 꺼내보자.
// 회원의 pk가 필요할 때 이거 사용해서 꺼내써야 함.

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

/* 사용방법
*  Service, Controller 에서 호출할 때

Long currentMemberId = SecurityUtil.getCurrentMemberId();

if (currentMemberId != null) {
    System.out.println("현재 로그인한 사용자 PK: " + currentMemberId);
} else {
    System.out.println("로그인한 사용자 없음");
}
* */



