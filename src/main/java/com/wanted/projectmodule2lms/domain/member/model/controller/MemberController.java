package com.wanted.projectmodule2lms.domain.member.model.controller;

import com.wanted.projectmodule2lms.domain.member.model.dto.SignupDTO;
import com.wanted.projectmodule2lms.domain.member.model.service.MemberService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/signup")
    public void signup(){ }

    // 파일 업로드를 받기 위해 MultipartFile 추가, Redirect 처리를 위해 String 리턴 사용
    @AuditLog
    @PostMapping("/signup")
    public String signup(@ModelAttribute SignupDTO signupDTO,
                         @RequestParam(value = "gradCert", required = false) MultipartFile gradCert,
                         @RequestParam(value = "careerCert", required = false) MultipartFile careerCert,
                         Model model, RedirectAttributes rttr) {

        /*
         * TODO: memberService.regist() 메서드에서 파일을 처리할 수 있도록 수정이 필요
         * 예: memberService.regist(signupDTO, gradCert, careerCert);
         */
        Integer result = memberService.regist(signupDTO, gradCert, careerCert); // 임시로 기존 로직 유지

        if(result == null) {
            model.addAttribute("message", "중복 ID를 가진 회원이 존재합니다.");
            return "member/signup"; // 가입 실패 시 뷰만 다시 렌더링 (입력 데이터 유지됨)

        } else if(result == 0) {
            model.addAttribute("message", "서버에서 오류가 발생하였습니다.");
            return "member/signup";

        } else {
            // 가입 성공 시: RedirectAttributes의 Flash 기능을 사용하면
            // 리다이렉트 된 직후의 페이지(login.html)까지 딱 한 번만 데이터가 살아서 전달돼!
            rttr.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/auth/login"; // 새로고침 버그 방지를 위해 반드시 redirect!
        }
    }

}
