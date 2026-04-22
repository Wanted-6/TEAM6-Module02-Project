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

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMemberId.class);
        boolean isLongType = Long.class.isAssignableFrom(parameter.getParameterType());

        return hasAnnotation && isLongType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthDetails authDetails) {
            LoginMemberDTO loginMember = authDetails.getLoginMemberDTO();

            if (loginMember != null && loginMember.getMemberId() != null) {
                return loginMember.getMemberId().longValue();
            }
        }

        return null;
    }
}
