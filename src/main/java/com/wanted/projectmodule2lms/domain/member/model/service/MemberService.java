package com.wanted.projectmodule2lms.domain.member.model.service;

import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import com.wanted.projectmodule2lms.domain.member.model.dto.SignupDTO;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
            String savedGradPath = null;
            String savedCareerPath = null;

            if ("INSTRUCTOR".equals(signupDTO.getMemberRole())) {
                savedGradPath = saveFile(gradCert);
                savedCareerPath = saveFile(careerCert);
            }

            Member member = Member.builder()
                    .loginId(signupDTO.getMemberId())
                    .password(encoder.encode(signupDTO.getMemberPassword())) // 생성할 때 바로 암호화
                    .name(signupDTO.getMemberName())
                    .email(signupDTO.getMemberEmail())
                    .phone(signupDTO.getMemberPhone())
                    .role(MemberRole.valueOf(signupDTO.getMemberRole()))
                    .gradCertPath(savedGradPath)
                    .careerCertPath(savedCareerPath)
                    .build();

//            // 첨부파일 확인 로직
//            if ("INSTRUCTOR".equals(signupDTO.getMemberRole())) {
//                if (gradCert != null && !gradCert.isEmpty()) {
//                    System.out.println("졸업증명서 업로드 대기 중: " + gradCert.getOriginalFilename());
//                }
//                if (careerCert != null && !careerCert.isEmpty()) {
//                    System.out.println("경력증명서 업로드 대기 중: " + careerCert.getOriginalFilename());
//                }
//            }

            Member savedMember = memberRepository.save(member);
            return savedMember.getMemberId();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public LoginMemberDTO findByUsername(String username) {
        Optional<Member> memberOptional = memberRepository.findByLoginId(username);

        // Entity -> DTO 변환도 직접 매핑 (DTO 생성자나 빌더 활용)
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
            loginMemberDTO.setTempPassword(member.isTempPassword());
            loginMemberDTO.setApprovalStatus(member.getApprovalStatus().name());
            loginMemberDTO.setVerified(member.getIsVerified());
            loginMemberDTO.setRejectReason(member.getRejectReason());

            return loginMemberDTO;
        }
        return null;
    }

//    @Transactional
//    public void incrementLoginFailCount(String username) {
//        Optional<Member> memberOptional = memberRepository.findByLoginId(username);
//
//        if (memberOptional.isPresent()) {
//            Member member = memberOptional.get();
//
//            member.increaseLoginFailCount();
//
//            if (member.getLoginFailCount() >= 5) {
//                member.lockAccount(); // 5회 이상이면 계정 잠금
//            }
//        }
//    }

    @Transactional
    public int incrementLoginFailCount(String username) {
        // 엔티티를 직접 가져온다고 가정 (레포지토리 메서드명은 네 코드에 맞게 수정해!)
        Member member = memberRepository.findByLoginId(username).orElse(null);

        if (member != null) {
            member.increaseLoginFailCount(); // 숫자 1 올리기 (+ null 방어)
            return member.getLoginFailCount(); // 방금 올라간 그 숫자를 바로 던져줌!
        }
        return 0; // 회원이 없으면 0
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
        // 기존에 만들어둔 findByUsername으로 회원 정보 가져오기
        LoginMemberDTO member = findByUsername(username);

        // 회원이 존재하고 실패 횟수가 null이 아니면 그 값을 반환, 아니면 0 반환
        if (member != null && member.getLoginFailCount() != null) {
            return member.getLoginFailCount();
        }
        return 0;
    }

    // ID 중복 체크
    public boolean checkIdDuplicate(String memberId){
        return memberRepository.existsByLoginId(memberId);
    }

    // 이메일 중복 체크
    public boolean checkEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 전화번호 중복 체크
    public boolean checkPhoneDuplicate(String phone) {
        return memberRepository.existsByPhone(phone);
    }


    // 아이디 찾기
    public String findLoginIdByNameAndEmail(String name, String email){
        Optional<Member> member = memberRepository.findByNameAndEmail(name, email);

        if (member.isPresent()) {
            String loginId = member.get().getLoginId();
            // 아이디 일부분 마스킹 하기
            return maskLoginId(loginId);
        }
        return null;  // 못 찾았을 경우
    }

    // 아이디 마스킹 처리하기
    private String maskLoginId(String loginId){
        if (loginId.length() <= 3){
            return loginId.substring(0, 1) + "**";
        }
        return loginId.substring(0, 3) + "*".repeat(loginId.length()-3);
    }

    // 비밀번호 찾기 (임시 비밀번호 발급)
    @Transactional
    public String issueTempPassword(String loginId, String email){
        Optional<Member> memberOpt = memberRepository.findByLoginIdAndEmail(loginId, email);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            // 임시 비밀번호 생성 (비밀번호 규칙 지켜서 )
            // 비밀번호 복잡도 검사 통과 위해 '!A1' 강제로 붙임.(비밀번호 조건 통과 위해)
             String tempPassword = UUID.randomUUID().toString().substring(0, 6) + "!A1";

             member.changeToTempPassword(encoder.encode(tempPassword));

             // 비밀번호 암호화 후 DB에 넣기
//            member.changePassword(encoder.encode(tempPassword));

            return tempPassword;
        }
        return null;
    }

    @Transactional
    public void changeRegularPassword(Long memberId, String newPassword){

        Member member = memberRepository.findById(Math.toIntExact(memberId))
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        member.changeRegularPassword(encoder.encode(newPassword));
    }

    private String saveFile(MultipartFile file){
        if (file == null || file.isEmpty()) return null;

        try {
            String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // 덮어쓰기 방지를 위해 파일 이름 앞 무작위 영문자(UUID) 붙임
            String storedFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            File targetFile = new File(dir, storedFileName);

            file.transferTo(targetFile); // 실제 컴퓨터 하드디스크에 파일 복사!

            // 나중에 HTML에서 다운로드 링크로 쓸 수 있는 주소 반환
            return "/uploads/" + storedFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}