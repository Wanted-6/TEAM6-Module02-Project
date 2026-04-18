package com.wanted.projectmodule2lms.domain.member.model.controller;

import com.wanted.projectmodule2lms.domain.course.service.CourseService;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.service.EnrollmentService;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.dto.SignupDTO;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    @GetMapping("/signup")
    public void signup(){ }

    @AuditLog
    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupDTO signupDTO,
                         @RequestParam(value = "gradCert", required = false) MultipartFile gradCert,
                         @RequestParam(value = "careerCert", required = false) MultipartFile careerCert,
                         Model model, RedirectAttributes rttr) {

        Integer result = memberService.regist(signupDTO, gradCert, careerCert);

        if(result == null) {
            model.addAttribute("message", "중복 ID를 가진 회원이 존재합니다.");
            return "member/signup";

        } else if(result == 0) {
            model.addAttribute("message", "서버에서 오류가 발생하였습니다.");
            return "member/signup";

        } else {
            rttr.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/edit-password")
    public String editPasswordForm(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
            return "redirect:/auth/login";
        }
        return "member/edit-password";
    }

    @AuditLog
    @PostMapping("/edit-password")
    public String updatePassword(@LoginMemberId Long memberId, @RequestParam("newPassword") String newPassword) {
        if(memberId == null) {
            return "redirect:/auth/login";
        }
        memberService.changeRegularPassword(memberId, newPassword);
        return "redirect:/";
    }

    @GetMapping("verify-code")
    public String verifyCodeForm(@LoginMemberId Long memberId) {
        if (memberId == null) return "redirect:/auth/login";
        return "member/verify-code";
    }

    @AuditLog
    @PostMapping("/verify-code")
    public String processVerifyCode(@LoginMemberId Long memberId, @RequestParam("code") String inputCode, RedirectAttributes rttr) {
        if (memberId == null) return "redirect:/auth/login";

        Member member = memberRepository.findById(Math.toIntExact(memberId)).orElseThrow();

        if (inputCode.equals(member.getApprovalCode())) {
            member.verifyApprovalCode();
            memberRepository.save(member);
            rttr.addFlashAttribute("message", "강사 인증이 완료되었습니다.");
            return "redirect:/";
        } else {
            rttr.addFlashAttribute("error", "승인 코드가 올바르지 않습니다.");
            return "redirect:/member/verify-code";
        }
    }

    @GetMapping("/mypage")
    public String myPage(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
            return "redirect:/auth/login";
        }

        Member member = memberRepository.findById(Math.toIntExact(memberId)).orElseThrow();

        // ⭐️ [수정 포인트] model.addAttribute("member", member); 를 삭제했습니다.
        // 이제 GlobalControllerAdvice의 @ModelAttribute("member")가 최신 데이터를 공급합니다.

        if (member.getRole() == MemberRole.STUDENT) {
            List<Enrollment> enrollments = enrollmentService.getMyEnrollments(memberId.intValue());

            List<Map<String, Object>> courseList = enrollments.stream().map(enroll -> {
                Map<String, Object> map = new HashMap<>();
                map.put("enrollmentId", enroll.getEnrollmentId());
                String cName = courseService.getCourseNameById(enroll.getCourseId());
                map.put("courseName", cName != null ? cName : "알 수 없는 과목");
                return map;
            }).collect(Collectors.toList());

            model.addAttribute("enrolledCourses", courseList);
        }

        return "member/mypage";
    }

    @AuditLog
    @PostMapping("/profile/update")
    public String updateProfile(@LoginMemberId Long memberId,
                                @RequestParam("bio") String bio,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                RedirectAttributes rttr) throws IOException {

        memberService.updateProfile(memberId, bio, profileImage);

        rttr.addFlashAttribute("message", "프로필이 수정되었습니다.");
        return "redirect:/member/mypage";
    }

    @AuditLog
    @PostMapping("/phone/update")
    public String updatePhone(@LoginMemberId Long memberId, @RequestParam("phone") String phone, RedirectAttributes rttr) {
        try {
            memberService.updatePhone(memberId, phone);
            rttr.addFlashAttribute("message", "전화번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/member/mypage";
    }

    @AuditLog
    @PostMapping("/delete")
    public String deleteMember(@LoginMemberId Long memberId, HttpSession session, RedirectAttributes rttr) {
        if (memberId == null) return "redirect:/auth/login";

        try {
            memberService.deleteMember(memberId);
            session.invalidate();
            return "redirect:/";
        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/mypage";
        }
    }
}
