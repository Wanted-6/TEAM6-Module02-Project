package com.wanted.projectmodule2lms.global.resolver;

import com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    // 작동할 조건 설정
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMemberId.class);
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isLongType;
    }

    // 조건이 맞으면 실제로 파라미터에 뭘 넣어줄지 결정
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        // 시큐리티 세션에서 현재 인증 정보 꺼내기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("========== 디버깅 시작 ==========");
        System.out.println("1. 인증 객체: " + authentication);

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("2. 실패: 인증 안 됨! (anonymousUser)");
            return null;
        }
        Object principal = authentication.getPrincipal();
        System.out.println("3. principal 클래스: " + principal.getClass().getName()); // 3번 출력

        if (principal instanceof AuthDetails authDetails) {
            LoginMemberDTO loginMember = authDetails.getLoginMemberDTO();
            System.out.println("4. DTO 안의 memberId 값: " + loginMember.getMemberId()); // 4번 출력

            if (loginMember != null && loginMember.getMemberId() != null) {
                return loginMember.getMemberId().longValue();
            }
        }

        System.out.println("5. 실패: AuthDetails가 아니거나 memberId가 null임!");
        return null;
    }
}
