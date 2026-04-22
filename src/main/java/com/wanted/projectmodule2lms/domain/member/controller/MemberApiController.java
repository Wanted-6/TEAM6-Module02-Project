package com.wanted.projectmodule2lms.domain.member.controller;

import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String memberId) { // 파라미터 이름 memberId로 통일!
        return ResponseEntity.ok(memberService.checkIdDuplicate(memberId));
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(memberService.checkEmailDuplicate(email));
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhone(@RequestParam String phone) {
        return ResponseEntity.ok(memberService.checkPhoneDuplicate(phone));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@LoginMemberId Long memberId, @RequestBody Map<String, String> request) {
        if (memberId == null) {
            return ResponseEntity.ok(false);
        }

        String currentPassword = request.get("password");

        boolean isMatch = memberService.verifyPassword(memberId, currentPassword);

        return ResponseEntity.ok(isMatch);
    }
}
