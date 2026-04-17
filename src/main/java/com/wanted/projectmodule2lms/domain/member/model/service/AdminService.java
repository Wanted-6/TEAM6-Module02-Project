package com.wanted.projectmodule2lms.domain.member.model.service;

import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;

    //강사 승인 및 코드 발급
    @Transactional
    public String approveInstructor(Integer memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 6자리로 랜덤 승인 코드 생성
        String approvalCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 엔티티의 메서드 호출해서 상태 변경함
        member.approveInstructor(approvalCode);

        // TODO: 여기서 발급된 approvalCode를 강사의 이메일로 전송하는 로직 추가!
        // emailService.sendApprovalCode(member.getEmail(), approvalCode);

        return approvalCode;
    }

    // 강사 반려
    @Transactional
    public void rejectInstructor(Integer memberId, String reason) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        member.rejectInstructor(reason);

        // TODO: 여기서 반려 사유(reason)를 강사의 이메일로 전송하는 로직 추가!
        // emailService.sendRejectEmail(member.getEmail(), reason);
    }
}