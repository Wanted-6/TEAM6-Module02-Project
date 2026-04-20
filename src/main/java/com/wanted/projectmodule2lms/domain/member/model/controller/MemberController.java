package com.wanted.projectmodule2lms.domain.member.model.controller;

import com.wanted.projectmodule2lms.domain.course.model.dao.CourseRepository;
import com.wanted.projectmodule2lms.domain.course.model.entity.Course;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            rttr.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("message", e.getMessage());
            return "member/signup";
        } catch (Exception e) {
            model.addAttribute("message", "서버에서 오류가 발생하였습니다.");
            return "member/signup";
        }
    }

    @GetMapping("/edit-password")
    public String editPasswordForm(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
            return "redirect:/auth/login";
        }
        return "member/edit-password";
    }

    @PostMapping("/edit-password")
    public String updatePassword(@LoginMemberId Long memberId,
                                 @RequestParam("newPassword") String newPassword,
                                 HttpSession session,
                                 RedirectAttributes rttr) {

        if (memberId == null) {
            return "redirect:/auth/login";
        }

        try {
            memberService.changeRegularPassword(memberId, newPassword);
            session.invalidate();

            rttr.addFlashAttribute("message", "비밀번호가 성공적으로 변경되었습니다. 새로운 비밀번호로 다시 로그인해주세요.");
            return "redirect:/auth/login";

        } catch (Exception e) {
            rttr.addFlashAttribute("error", "비밀번호 변경에 실패했습니다.");
            return "redirect:/member/edit-password";
        }
    }

    @GetMapping("verify-code")
    public String verifyCodeForm(@LoginMemberId Long memberId) {
        if (memberId == null) return "redirect:/auth/login";
        return "member/verify-code";
    }

    @PostMapping("/verify-code")
    public String processVerifyCode(@LoginMemberId Long memberId, @RequestParam("code") String inputCode, RedirectAttributes rttr) {
        if (memberService.verifyInstructorCode(memberId, inputCode)) {
            rttr.addFlashAttribute("message", "강사 인증이 완료되었습니다.");
            return "redirect:/";
        } else {
            rttr.addFlashAttribute("error", "승인 코드가 올바르지 않습니다.");
            return "redirect:/member/verify-code";
        }
    }

    @AuditLog
    @GetMapping("/mypage")
    public String myPage(@LoginMemberId Long memberId, Model model) {
        if (memberId == null) {
            return "redirect:/auth/login";
        }

        Member member = memberRepository.findById(Math.toIntExact(memberId)).orElseThrow();

        if (member.getRole() == MemberRole.STUDENT) {
            List<Enrollment> enrollments = enrollmentService.getMyEnrollments(memberId.intValue());

            //코드 최적화 전.
//            List<Map<String, Object>> courseList = enrollments.stream().map(enroll -> {
//                Map<String, Object> map = new HashMap<>();
//                map.put("enrollmentId", enroll.getEnrollmentId());
//                String cName = courseService.getCourseNameById(enroll.getCourseId());
//                map.put("courseName", cName != null ? cName : "알 수 없는 과목");
//                return map;
//            }).collect(Collectors.toList());

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

        if (useDefaultImage) {
            memberService.updateToDefaultProfile(memberId, bio);
        } else {
            memberService.updateProfile(memberId, bio, profileImage);
        }

        rttr.addFlashAttribute("message", "프로필이 수정되었습니다.");
        return "redirect:/member/mypage";
    }

    @PostMapping("/phone/update")
    @ResponseBody
    public ResponseEntity<String> updatePhone(@LoginMemberId Long memberId, @RequestParam("phone") String phone) {
        if (memberId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }
        try {
            memberService.updatePhone(memberId, phone);
            return ResponseEntity.ok("success");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("fail");
        }
    }

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
