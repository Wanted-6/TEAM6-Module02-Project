package com.wanted.projectmodule2lms.domain.member.model.service;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.enrollment.model.dao.EnrollmentRepository;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import com.wanted.projectmodule2lms.domain.member.model.dto.SignupDTO;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.profile.dao.ProfileRepository;
import com.wanted.projectmodule2lms.domain.profile.entity.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
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
                    .password(encoder.encode(signupDTO.getMemberPassword()))
                    .name(signupDTO.getMemberName())
                    .email(signupDTO.getMemberEmail())
                    .phone(signupDTO.getMemberPhone())
                    .role(MemberRole.valueOf(signupDTO.getMemberRole()))
                    .gradCertPath(savedGradPath)
                    .careerCertPath(savedCareerPath)
                    .build();

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
            loginMemberDTO.setTempPassword(member.isTempPassword());
            loginMemberDTO.setApprovalStatus(member.getApprovalStatus().name());
            loginMemberDTO.setVerified(member.getIsVerified());
            loginMemberDTO.setRejectReason(member.getRejectReason());

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
        memberOptional.ifPresent(Member::resetLoginFailCount);
    }

    public int getLoginFailCount(String username) {
        LoginMemberDTO member = findByUsername(username);
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
            return maskLoginId(loginId);
        }
        return null;
    }

    // 아이디 마스킹 처리
    private String maskLoginId(String loginId){
        if (loginId.length() <= 3){
            return loginId.substring(0, 1) + "**";
        }
        return loginId.substring(0, 3) + "*".repeat(loginId.length() - 3);
    }

    // 비밀번호 찾기 (임시 비밀번호 발급)
    @Transactional
    public String issueTempPassword(String loginId, String email){
        Optional<Member> memberOpt = memberRepository.findByLoginIdAndEmail(loginId, email);

        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            String tempPassword = UUID.randomUUID().toString().substring(0, 6) + "!A1";
            member.changeToTempPassword(encoder.encode(tempPassword));
            return tempPassword;
        }
        return null;
    }

    // 비밀번호 변경
    @Transactional
    public void changeRegularPassword(Long memberId, String newPassword){
        Member member = memberRepository.findById(Math.toIntExact(memberId))
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        member.changeRegularPassword(encoder.encode(newPassword));
    }

    @Transactional(readOnly = true)
    public boolean verifyPassword(Long memberId, String currentPassword) {
        Member member = memberRepository.findById(Math.toIntExact(memberId))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return encoder.matches(currentPassword, member.getPassword());
    }

    private String saveFile(MultipartFile file){
        if (file == null || file.isEmpty()) return null;

        try {
            String uploadDir = "C:/lab/uploads/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String storedFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            File targetFile = new File(dir, storedFileName);

            file.transferTo(targetFile);

            return "/uploads/" + storedFileName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 프로필 업데이트 (사진 첨부 시)
    @Transactional
    public void updateProfile(Long memberId, String bio, MultipartFile file) throws IOException {
        Member member = memberRepository.findById(Math.toIntExact(memberId))
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Profile profile = member.getProfile();

        if (profile == null) {
            profile = Profile.builder()
                    .member(member)
                    .bio(bio)
                    .profileImage("default-profile.png")
                    .build();
            member.assignProfile(profile);
        }

        if (file != null && !file.isEmpty()) {
            String savePath = "C:/lab/uploads/"; // 환경에 맞게 경로 확인 요망
            File dir = new File(savePath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            file.transferTo(new File(savePath + fileName));

            profile.update(bio, fileName);
        } else {
            profile.update(bio, profile.getProfileImage());
        }

        profileRepository.save(profile);
    }

    @Transactional
    public void updateToDefaultProfile(Long memberId, String bio) {
        Member member = memberRepository.findById(Math.toIntExact(memberId))
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        Profile profile = member.getProfile();

        if (profile == null) {
            // 프로필이 아예 없었다면 새로 생성하면서 기본 이미지 셋팅
            profile = Profile.builder()
                    .member(member)
                    .bio(bio)
                    .profileImage("default-profile.png")
                    .build();
            member.assignProfile(profile);
        } else {
            // 기존 프로필이 있다면 이미지 파일명을 기본으로 업데이트
            profile.update(bio, "default-profile.png");
        }

        profileRepository.save(profile);
    }

    // 전화번호 업데이트
    @Transactional
    public void updatePhone(Long memberId, String phone) {
        Member member = memberRepository.findById(Math.toIntExact(memberId)).orElseThrow();

        if (member.getPhone() != null && member.getPhone().equals(phone)) return;
        if (memberRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
        }
        member.updatePhone(phone);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(Long memberId) {
        Integer memberIdInt = Math.toIntExact(memberId);

        Member member = memberRepository.findById(memberIdInt)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 강사 탈퇴 조건
        if (member.getRole() == MemberRole.INSTRUCTOR) {
            boolean hasActiveCourses = courseRepository.existsByInstructorIdAndIsOpenTrue(memberIdInt);
            if (hasActiveCourses) {
                throw new IllegalStateException("진행 중인 강의가 있어 탈퇴할 수 없습니다. 강의 종료 후 다시 시도해 주세요.");
            }
        }
        // 학생 탈퇴 조건
        else if (member.getRole() == MemberRole.STUDENT) {
            boolean hasActiveEnrollments = enrollmentRepository.existsByMemberId(memberIdInt);
            if (hasActiveEnrollments) {
                throw new IllegalStateException("수강 중인 강의가 있어 탈퇴할 수 없습니다. 수강 취소 또는 종료 후 다시 시도해 주세요.");
            }
        }

        memberRepository.delete(member);
    }
}
