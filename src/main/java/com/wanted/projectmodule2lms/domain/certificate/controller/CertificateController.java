package com.wanted.projectmodule2lms.domain.certificate.controller;

import com.wanted.projectmodule2lms.domain.certificate.model.dto.CertificateViewDTO;
import com.wanted.projectmodule2lms.domain.certificate.model.service.CertificateService;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import com.wanted.projectmodule2lms.global.exception.LoginRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
@RequestMapping("/student/certificate")
public class CertificateController {

    private final CertificateService certificateService;


    @PostMapping("/request")
    public String requestCertificate(@LoginMemberId Long memberId,
                                     @RequestParam Integer courseId) {
        certificateService.requestCertificate(requireMemberId(memberId), courseId);
        Integer sectionId = certificateService.findFirstSectionId(courseId);

        if (sectionId == null) {
            return "redirect:/student/courses";
        }

        return "redirect:/student/attendance/" + courseId + "/" + sectionId;
    }
    @GetMapping
    public String certificatePage(@LoginMemberId Long memberId,
                                  @RequestParam Integer courseId,
                                  Model model) {
        CertificateViewDTO certificate = certificateService.findCertificateForStudent(requireMemberId(memberId), courseId);
        model.addAttribute("certificate", certificate);
        model.addAttribute("courseId", courseId);
        return "student/certificate/view";
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadCertificate(@LoginMemberId Long memberId,
                                                      @RequestParam Integer courseId) {
        try {
            byte[] pdfBytes = certificateService.generateCertificatePdf(requireMemberId(memberId), courseId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (LoginRequiredException e) {
            return ResponseEntity.status(302)
                    .header(HttpHeaders.LOCATION, "/auth/login")
                    .build();
        }
    }

    private Integer requireMemberId(Long memberId) {
        if (memberId == null) {
            throw new LoginRequiredException("로그인한 사용자 정보가 필요합니다.");
        }
        return memberId.intValue();
    }
}
