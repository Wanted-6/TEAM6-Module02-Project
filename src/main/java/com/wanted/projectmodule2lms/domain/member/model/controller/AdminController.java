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
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

        int activeUserCount = 0;
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            List<SessionInformation> sessions = sessionRegistry.getAllSessions(principal, false);
            if (!sessions.isEmpty()) {
                activeUserCount++;
            }
        }

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
        String code = adminService.approveInstructor(memberId);

        rttr.addFlashAttribute("message", "승인 완료! 발급된 코드: [" + code + "]");
        return "redirect:/admin/instructors";
    }

    @PostMapping("/instructors/{memberId}/reject")
    public String rejectInstructor(@PathVariable Integer memberId, @RequestParam("reason") String reason, RedirectAttributes rttr) {
        adminService.rejectInstructor(memberId, reason);
        rttr.addFlashAttribute("message", "반려 처리되었습니다. 사유: " + reason);
        return "redirect:/admin/instructors";
    }

    // 메서드 성능 통계 조회
    @AuditLog
    @GetMapping("/audit-logs")
    public String showAuditLogStatistics(Model model) {
        List<LogStatDto> stats = auditLogRepository.findMethodStatistics();
        model.addAttribute("logStats", stats);
        return "admin/audit-logs";
    }

}
