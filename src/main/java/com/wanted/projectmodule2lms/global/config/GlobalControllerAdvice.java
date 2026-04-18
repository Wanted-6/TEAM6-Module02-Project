package com.wanted.projectmodule2lms.global.config;

import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final MemberRepository memberRepository;

    @ModelAttribute("member")
    public Member addGlobalMember(@LoginMemberId Long memberId) {
        if (memberId != null) {
            return memberRepository.findById(Math.toIntExact(memberId)).orElse(null);
        }
        return null;
    }
}
