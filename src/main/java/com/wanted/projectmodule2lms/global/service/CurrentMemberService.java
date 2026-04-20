package com.wanted.projectmodule2lms.global.service;

import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentMemberService {

    private final MemberRepository memberRepository;

    public Integer getCurrentMemberId() {
        Long currentMemberId = SecurityUtil.getCurrentMemberId();
        return currentMemberId != null ? currentMemberId.intValue() : null;
    }

    public MemberRole getCurrentMemberRole() {
        Integer currentMemberId = getCurrentMemberId();

        return getCurrentMemberRole(currentMemberId);
    }

    public MemberRole getCurrentMemberRole(Integer currentMemberId) {

        if (currentMemberId == null) {
            return null;
        }

        Member member = memberRepository.findById(currentMemberId).orElse(null);
        return member != null ? member.getRole() : null;
    }

    public Integer toMemberId(Long loginMemberId) {
        return loginMemberId != null ? loginMemberId.intValue() : null;
    }
}
