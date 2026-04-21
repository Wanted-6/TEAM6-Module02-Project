package com.wanted.projectmodule2lms.domain.member.model.controller;

import com.wanted.projectmodule2lms.domain.member.model.dao.LoginLogRepository;
import com.wanted.projectmodule2lms.domain.member.model.dao.MemberRepository;
import com.wanted.projectmodule2lms.domain.member.model.entity.LoginLog;
import com.wanted.projectmodule2lms.domain.member.model.entity.Member;
import com.wanted.projectmodule2lms.domain.member.model.entity.MemberRole;
import com.wanted.projectmodule2lms.domain.member.model.service.AdminService;
import com.wanted.projectmodule2lms.global.annotation.AuditLog;
import com.wanted.projectmodule2lms.global.aop.dao.AuditLogRepository;
import com.wanted.projectmodule2lms.global.aop.dto.LogStatDto;
import com.wanted.projectmodule2lms.global.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final LoginLogRepository loginLogRepository;
    private final SessionRegistry sessionRegistry;
    private final AdminService adminService;
    private final MemberRepository memberRepository;
    private final AuditLogRepository auditLogRepository;

    @AuditLog
    @GetMapping("/logs")
    public String getLoginLogs(Model model) {
        List<LoginLog> logs = loginLogRepository.findAllByOrderByLoginTimeDesc();
        int activeUserCount = adminService.getActiveUserCount(sessionRegistry);

        model.addAttribute("logs", logs);
        model.addAttribute("activeUsers", activeUserCount);
        return "admin/logs";
    }

    @AuditLog
    @GetMapping("/instructors")
    public String pendingInstructors(Model model){
        List<Member> allInstructors = memberRepository.findByRole(MemberRole.INSTRUCTOR);

        model.addAttribute("instructors", allInstructors);

        return "admin/instructor-list";
    }

    @PostMapping("/instructors/{memberId}/approve")
    public String approveInstructor(@PathVariable Integer memberId, RedirectAttributes rttr) {
        try {
            String code = adminService.approveInstructor(memberId);
            rttr.addFlashAttribute("message", "승인 완료! 발급된 코드: [" + code + "]");
        } catch (ResourceNotFoundException e) {
            rttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/instructors";
    }

    @PostMapping("/instructors/{memberId}/reject")
    public String rejectInstructor(@PathVariable Integer memberId,
                                   @RequestParam("reason") String reason,
                                   RedirectAttributes rttr) {
        try {
            adminService.rejectInstructor(memberId, reason);
            rttr.addFlashAttribute("message", "반려 처리되었습니다. 사유: " + reason);
        } catch (ResourceNotFoundException e) {
            rttr.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/instructors";
    }

    @AuditLog
    @GetMapping("/audit-logs")
    public String showAuditLogStatistics(Model model) {
        List<LogStatDto> stats = auditLogRepository.findMethodStatistics();
        model.addAttribute("logStats", stats);
        return "admin/audit-logs";
    }
}