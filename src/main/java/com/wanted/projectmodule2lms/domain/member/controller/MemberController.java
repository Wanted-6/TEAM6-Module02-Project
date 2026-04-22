package com.wanted.projectmodule2lms.domain.member.controller;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
import com.wanted.projectmodule2lms.domain.enrollment.model.entity.Enrollment;
import com.wanted.projectmodule2lms.domain.enrollment.model.service.EnrollmentService;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.dto.SignupDTO;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final EnrollmentService enrollmentService;
    private final CourseRepository courseRepository;

    @GetMapping("/signup")
    public void signup() {
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupDTO signupDTO,
                         @RequestParam(value = "gradCert", required = false) MultipartFile gradCert,
                         @RequestParam(value = "careerCert", required = false) MultipartFile careerCert,
                         Model model, RedirectAttributes rttr) {
        try {
            memberService.regist(signupDTO, gradCert, careerCert);
            rttr.addFlashAttribute("message", "회원가입이 완료되었습니다. 승인 후 로그인하실 수 있습니다.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("message", e.getMessage());
            return "member/signup";
        } catch (Exception e) {
            model.addAttribute("message", "회원가입 처리 중 오류가 발생했습니다.");
            return "member/signup";
        }
    }

    @GetMapping("/edit-password")
    public String editPasswordForm(@LoginMemberId Long memberId, Model model) {
        requireMemberId(memberId);
        return "member/edit-password";
    }

    @PostMapping("/edit-password")
    public String updatePassword(@LoginMemberId Long memberId,
                                 @RequestParam("newPassword") String newPassword,
                                 HttpSession session,
                                 RedirectAttributes rttr) {
        try {
            memberService.changeRegularPassword(requireMemberId(memberId), newPassword);
            session.invalidate();

            rttr.addFlashAttribute("message", "비밀번호가 변경되었습니다. 보안을 위해 다시 로그인해주세요.");
            return "redirect:/auth/login";
        } catch (ResourceNotFoundException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/edit-password";
        } catch (Exception e) {
            rttr.addFlashAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
            return "redirect:/member/edit-password";
        }
    }

    @GetMapping("verify-code")
    public String verifyCodeForm(@LoginMemberId Long memberId) {
        requireMemberId(memberId);
        return "member/verify-code";
    }

    @PostMapping("/verify-code")
    public String processVerifyCode(@LoginMemberId Long memberId,
                                    @RequestParam("code") String inputCode,
                                    RedirectAttributes rttr) {
        if (memberService.verifyInstructorCode(requireMemberId(memberId), inputCode)) {
            rttr.addFlashAttribute("message", "강사 인증이 완료되었습니다.");
            return "redirect:/";
        } else {
            rttr.addFlashAttribute("error", "인증 코드가 올바르지 않습니다.");
            return "redirect:/member/verify-code";
        }
    }

    @AuditLog
    @GetMapping("/mypage")
    public String myPage(@LoginMemberId Long memberId, Model model) {
        Long currentMemberId = requireMemberId(memberId);

        Member member = memberRepository.findById(currentMemberId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("회원 정보를 찾을 수 없습니다."));

        if (member.getRole() == MemberRole.STUDENT) {
            List<Enrollment> enrollments = enrollmentService.getMyEnrollments(memberId.intValue());

            List<Integer> courseIds = enrollments.stream()
                    .map(Enrollment::getCourseId)
                    .collect(Collectors.toList());

            List<Course> courses = courseRepository.findAllById(courseIds);

            Map<Integer, String> courseNameMap = courses.stream()
                    .collect(Collectors.toMap(Course::getCourseId, Course::getTitle));

            List<Map<String, Object>> courseList = enrollments.stream().map(enroll -> {
                Map<String, Object> map = new HashMap<>();
                map.put("enrollmentId", enroll.getEnrollmentId());
                map.put("courseName", courseNameMap.getOrDefault(enroll.getCourseId(), "알 수 없는 과목"));
                return map;
            }).collect(Collectors.toList());

            model.addAttribute("enrolledCourses", courseList);
        }

        return "member/mypage";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@LoginMemberId Long memberId,
                                @RequestParam("bio") String bio,
                                @RequestParam(value = "useDefaultImage", defaultValue = "false") boolean useDefaultImage,
                                @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                RedirectAttributes rttr) throws IOException {
        Long currentMemberId = requireMemberId(memberId);

        if (useDefaultImage) {
            memberService.updateToDefaultProfile(currentMemberId, bio);
        } else {
            memberService.updateProfile(currentMemberId, bio, profileImage);
        }

        rttr.addFlashAttribute("message", "프로필이 성공적으로 수정되었습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/phone/update")
    @ResponseBody
    public ResponseEntity<String> updatePhone(@LoginMemberId Long memberId, @RequestParam("phone") String phone) {
        try {
            memberService.updatePhone(requireMemberId(memberId), phone);
            return ResponseEntity.ok("success");
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

    @PostMapping("/delete")
    public String deleteMember(@LoginMemberId Long memberId, HttpSession session, RedirectAttributes rttr) {
        try {
            memberService.deleteMember(requireMemberId(memberId));
            session.invalidate();
            return "redirect:/";
        } catch (ResourceNotFoundException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/mypage";
        } catch (IllegalStateException e) {
            rttr.addFlashAttribute("error", e.getMessage());
            return "redirect:/member/mypage";
        }
    }

    private Long requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new LoginRequiredException("로그인이 필요합니다.");
        }
        return memberId;
    }
}
