package com.wanted.projectmodule2lms.global.config;

import com.wanted.projectmodule2lms.domain.auth.model.dto.AuthDetails;
import com.wanted.projectmodule2lms.domain.member.model.dto.LoginMemberDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping(value = {"/", "/main"})
    public String mainPage(@AuthenticationPrincipal AuthDetails authDetails, Model model) {
        boolean loggedIn = authDetails != null && authDetails.getLoginMemberDTO() != null;
        model.addAttribute("loggedIn", loggedIn);

        if (loggedIn) {
            LoginMemberDTO loginMemberDTO = authDetails.getLoginMemberDTO();

            String dashboardRole = loginMemberDTO.getRoleList().stream()
                    .filter(role -> "STUDENT".equals(role) || "INSTRUCTOR".equals(role) || "ADMIN".equals(role))
                    .findFirst()
                    .orElse("STUDENT");

            model.addAttribute("memberId", loginMemberDTO.getMemberId());
            model.addAttribute("memberName", loginMemberDTO.getName());
            model.addAttribute("dashboardRole", dashboardRole);
        }

        return "main/main";
    }
}
