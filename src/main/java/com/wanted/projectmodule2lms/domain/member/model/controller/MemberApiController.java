package com.wanted.projectmodule2lms.domain.member.model.controller;

import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    // ID 중복 확인 API
    @GetMapping("/check-id")
    public ResponseEntity<Boolean> checkId(@RequestParam String memberId) { // 파라미터 이름 memberId로 통일!
        return ResponseEntity.ok(memberService.checkIdDuplicate(memberId));
    }

    // 이메일 중복 확인 API
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(memberService.checkEmailDuplicate(email));
    }

    // 전화번호 중복 확인 API
    @GetMapping("/check-phone")
    public ResponseEntity<Boolean> checkPhone(@RequestParam String phone) {
        return ResponseEntity.ok(memberService.checkPhoneDuplicate(phone));
    }
}
