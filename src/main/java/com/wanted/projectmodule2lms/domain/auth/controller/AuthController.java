package com.wanted.projectmodule2lms.domain.auth.controller;

import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;

    @GetMapping("/login")
    public void login(){
    }

    @GetMapping("/fail")
    public ModelAndView loginFail(HttpSession session, ModelAndView mv) {

        // 1. 세션 금고에서 에러 메시지 꺼내기
        String message = (String) session.getAttribute("errorMessage");

        // 2. 꺼낸 뒤에는 금고에서 바로 지워버리기 (새로고침 했을 때 계속 남아있지 않도록!)
        if (message != null) {
            session.removeAttribute("errorMessage");
        } else {
            // 혹시라도 그냥 주소창에 /auth/fail을 직접 치고 들어온 사람을 위한 기본 메시지
            message = "아이디 또는 비밀번호를 확인해주세요.";
        }

        // 3. 화면으로 전달
        mv.addObject("message", message);
        mv.setViewName("auth/fail"); // 맨 앞의 '/'는 빼는 게 타임리프 경로 찾기에 더 좋아!
        return mv;
    }



    // 아이디 찾기
    @GetMapping("/find-id")
    public String findIdPage() {
        return "auth/find-id";
    }
    @AuditLog
    @PostMapping("/find-id")
    @ResponseBody
    public ResponseEntity<String> findIdProcess(@RequestParam String name, @RequestParam String email) {
        String maskedId = memberService.findLoginIdByNameAndEmail(name, email);

        if (maskedId != null) {
            return ResponseEntity.ok("회원님의 아이디는 [" + maskedId + "] 입니다.");
        } else {
            return ResponseEntity.badRequest().body("일치하는 회원 정보가 없습니다.");
        }
    }

    // 비밀번호 찾기
    @GetMapping("/find-pw")
    public String findPwPage() {
        return "auth/find-pw";
    }

    @AuditLog
    @PostMapping("/find-pw")
    @ResponseBody
    public ResponseEntity<String> findPwProcess(@RequestParam String loginId, @RequestParam String email) {
        String tempPassword = memberService.issueTempPassword(loginId, email);

        if (tempPassword != null) {
            return ResponseEntity.ok("발급된 임시 비밀번호: [" + tempPassword + "] 입니다. 로그인 후 반드시 변경해주세요.");
        } else {
            return ResponseEntity.badRequest().body("아이디 또는 이메일 정보가 일치하지 않습니다.");
        }
    }

}
