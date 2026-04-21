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

        String message = (String) session.getAttribute("errorMessage");

        if (message != null) {
            session.removeAttribute("errorMessage");
        } else {
            message = "아이디 또는 비밀번호를 확인해주세요.";
        }

        mv.addObject("message", message);
        mv.setViewName("auth/fail");
        return mv;
    }

    @GetMapping("/find-id")
    public String findIdPage() {
        return "auth/find-id";
    }

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

    @GetMapping("/find-pw")
    public String findPwPage() {
        return "auth/find-pw";
    }

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
