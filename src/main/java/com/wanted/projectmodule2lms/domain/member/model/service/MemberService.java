package com.wanted.projectmodule2lms.domain.member.model.service;

import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import com.wanted.projectmodule2lms.domain.member.model.dto.SignupDTO;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public Integer regist(SignupDTO signupDTO, MultipartFile gradCert, MultipartFile careerCert) {

        if (signupDTO.getMemberId() == null || signupDTO.getMemberId().length() < 4) {
            System.out.println("아이디는 4글자 이상으로 설정해주세요.");
            return null;
        }

        if (memberRepository.existsByLoginId(signupDTO.getMemberId())) {
            return null;
        }

        try {
            Member member = Member.builder()
                    .loginId(signupDTO.getMemberId())
                    .password(encoder.encode(signupDTO.getMemberPassword()))
                    .name(signupDTO.getMemberName())
                    .email(signupDTO.getMemberEmail())
                    .phone(signupDTO.getMemberPhone())
                    .role(MemberRole.valueOf(signupDTO.getMemberRole()))
                    .build();

            if ("INSTRUCTOR".equals(signupDTO.getMemberRole())) {
                if (gradCert != null && !gradCert.isEmpty()) {
                    System.out.println("졸업증명서 업로드 대기 중 " + gradCert.getOriginalFilename());
                }
                if (careerCert != null && !careerCert.isEmpty()) {
                    System.out.println("경력증명서 업로드 대기 중 " + careerCert.getOriginalFilename());
                }
            }

            Member savedMember = memberRepository.save(member);
            return savedMember.getMemberId();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public LoginMemberDTO findByUsername(String username) {
        Optional<Member> memberOptional = memberRepository.findByLoginId(username);

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            LoginMemberDTO loginMemberDTO = new LoginMemberDTO();
            loginMemberDTO.setMemberId(member.getMemberId());
            loginMemberDTO.setLoginId(member.getLoginId());
            loginMemberDTO.setName(member.getName());
            loginMemberDTO.setPassword(member.getPassword());
            loginMemberDTO.setRole(member.getRole().name());
            loginMemberDTO.setLoginFailCount(member.getLoginFailCount());
            loginMemberDTO.setAccountLocked(Boolean.TRUE.equals(member.getAccountLocked()));
            return loginMemberDTO;
        }
        return null;
    }

    @Transactional
    public int incrementLoginFailCount(String username) {
        Member member = memberRepository.findByLoginId(username).orElse(null);

        if (member != null) {
            member.increaseLoginFailCount();
            return member.getLoginFailCount();
        }
        return 0;
    }

    @Transactional
    public void resetLoginFailCount(String username) {
        Optional<Member> memberOptional = memberRepository.findByLoginId(username);

        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            member.resetLoginFailCount();
        }
    }

    public int getLoginFailCount(String username) {
        LoginMemberDTO member = findByUsername(username);

        if (member != null && member.getLoginFailCount() != null) {
            return member.getLoginFailCount();
        }
        return 0;
    }

    public boolean checkIdDuplicate(String memberId){
        return memberRepository.existsByLoginId(memberId);
    }

    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean checkPhoneDuplicate(String phone) {
        return memberRepository.existsByPhone(phone);
    }

    public String findLoginIdByNameAndEmail(String name, String email){
        Optional<Member> member = memberRepository.findByNameAndEmail(name, email);

        if (member.isPresent()) {
            String loginId = member.get().getLoginId();
            return maskLoginId(loginId);
        }
        return null;
    }

    private String maskLoginId(String loginId){
        if (loginId.length() <= 3){
            return loginId.substring(0, 1) + "**";
        }
        return loginId.substring(0, 3) + "*".repeat(loginId.length()-3);
    }

    @Transactional
    public String issueTempPassword(String loginId, String email){
        Optional<Member> memberOpt = memberRepository.findByLoginIdAndEmail(loginId, email);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            String tempPassword = UUID.randomUUID().toString().substring(0, 6) + "!A1";
            member.changePassword(encoder.encode(tempPassword));
            return tempPassword;
        }
        return null;
    }
}
