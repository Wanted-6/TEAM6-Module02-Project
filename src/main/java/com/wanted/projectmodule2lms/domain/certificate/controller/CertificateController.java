package com.wanted.projectmodule2lms.domain.certificate.controller;

import com.wanted.projectmodule2lms.domain.certificate.model.dto.CertificateViewDTO;
import com.wanted.projectmodule2lms.domain.certificate.model.service.CertificateService;
import com.wanted.projectmodule2lms.domain.section.model.dao.SectionRepository;
import com.wanted.projectmodule2lms.domain.section.model.entity.Section;
import com.wanted.projectmodule2lms.global.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/certificate")
public class CertificateController {

    private final CertificateService certificateService;
    private final SectionRepository sectionRepository;


    @GetMapping("/request")
    public String requestCertificate(@LoginMemberId Long memberId,
                                     @RequestParam Integer courseId) {
        if (memberId == null) {
            return "redirect:/auth/login";
        }

        certificateService.requestCertificate(memberId.intValue(), courseId);

        Section section = sectionRepository.findByCourseIdAndSectionOrder(courseId, 1)
                .orElseGet(() -> {
                    List<Section> sectionList = sectionRepository.findByCourseIdOrderBySectionOrderAsc(courseId);
                    if (sectionList.isEmpty()) {
                        return null;
                    }
                    return sectionList.get(0);
                });

        if (section == null) {
            return "redirect:/student/courses";
        }

        return "redirect:/student/attendance/" + courseId + "/" + section.getSectionId();
    }
    @GetMapping
    public String certificatePage(@LoginMemberId Long memberId,
                                  @RequestParam Integer courseId,
                                  Model model) {

        if (memberId == null) {
            return "redirect:/auth/login";
        }
        CertificateViewDTO certificate = certificateService.findCertificateForStudent(memberId.intValue(), courseId);
        model.addAttribute("certificate", certificate);
        model.addAttribute("courseId", courseId);
        return "student/certificate/view";
    }
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadCertificate(@LoginMemberId Long memberId,
                                                      @RequestParam Integer courseId) {
        if (memberId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        byte[] pdfBytes = certificateService.generateCertificatePdf(memberId.intValue(), courseId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
