package com.wanted.projectmodule2lms.domain.certificate.controller;

import com.wanted.projectmodule2lms.domain.certificate.model.service.CertificateService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/certificates")
public class AdminCertificateController {

    private final CertificateService certificateService;

    @GetMapping
    public String certificateListPage(Model model) {
        model.addAttribute("certificateList", certificateService.findAllCertificatesForAdmin());
        return "admin/certificate/list";
    }

    @PostMapping("/{certificateId}/approve")
    public String approveCertificate(@PathVariable Integer certificateId,
                                     @LoginMemberId Long adminId) {
        certificateService.approveCertificate(certificateId, adminId.intValue());
        return "redirect:/admin/certificates";
    }

}
