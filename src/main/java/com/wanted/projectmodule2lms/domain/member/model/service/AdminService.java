package com.wanted.projectmodule2lms.domain.member.model.service;

import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final MailService mailService;

    @Transactional
    public String approveInstructor(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다."));

        String approvalCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        member.approveInstructor(approvalCode);
        mailService.sendApprovalEmail(member.getEmail(), approvalCode);
        return approvalCode;
    }

    @Transactional
    public void rejectInstructor(Integer memberId, String reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("회원을 찾을 수 없습니다."));

        member.rejectInstructor(reason);
        mailService.sendRejectEmail(member.getEmail(), reason);
    }

    public int getActiveUserCount(SessionRegistry sessionRegistry) {
        int count = 0;
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            if (!sessions.isEmpty()) {
                count++;
            }
        }
        return count;
    }
}